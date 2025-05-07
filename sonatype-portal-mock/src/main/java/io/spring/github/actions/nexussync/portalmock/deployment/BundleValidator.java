/*
 * Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.github.actions.nexussync.portalmock.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * Validates bundles.
 *
 * @author Moritz Halbritter
 */
@Component
class BundleValidator {

	void validate(Path bundle) {
		if (!Files.exists(bundle)) {
			throw new DeploymentFailedException("Bundle at '%s' doesn't exist".formatted(bundle.toAbsolutePath()));
		}
		try (ZipFile zipFile = new ZipFile(bundle.toFile())) {
			Map<String, ? extends ZipEntry> entries = zipFile.stream()
				.collect(Collectors.toMap(ZipEntry::getName, (e) -> e));
			Set<String> artifacts = findArtifacts(entries);
			for (String artifact : artifacts) {
				checkArtifact(zipFile, artifact, entries);
			}
		}
		catch (ZipException ex) {
			throw new DeploymentFailedException("Unable to read zip file at '%s'".formatted(bundle.toAbsolutePath()),
					ex);
		}
		catch (IOException ex) {
			throw new DeploymentFailedException(
					"IOException while reading bundle at '%s'".formatted(bundle.toAbsolutePath()), ex);
		}
	}

	private void checkArtifact(ZipFile zipFile, String artifact, Map<String, ? extends ZipEntry> entries)
			throws IOException {
		String pomFile = artifact + ".pom";
		String jarFile = artifact + ".jar";
		String javadocFile = artifact + "-javadoc.jar";
		String sourcesFile = artifact + "-sources.jar";
		boolean hasPom = entries.containsKey(pomFile);
		if (!hasPom) {
			throw new DeploymentFailedException("No pom found for artifact '%s'".formatted(artifact));
		}
		boolean hasJar = entries.containsKey(jarFile);
		if (!hasJar) {
			throw new DeploymentFailedException("No jar found for artifact '%s'".formatted(artifact));
		}
		boolean hasJavadoc = entries.containsKey(javadocFile);
		if (!hasJavadoc) {
			throw new DeploymentFailedException("No javadoc found for artifact '%s'".formatted(artifact));
		}
		boolean hasSources = entries.containsKey(sourcesFile);
		if (!hasSources) {
			throw new DeploymentFailedException("No sources found for artifact '%s'".formatted(artifact));
		}
		verifyChecksums(zipFile, entries, pomFile, jarFile, javadocFile, sourcesFile);
		verifySignatures(entries, pomFile, jarFile, javadocFile, sourcesFile);
	}

	private void verifyChecksums(ZipFile zipFile, Map<String, ? extends ZipEntry> entries, String... files)
			throws IOException {
		for (String file : files) {
			String md5File = file + ".md5";
			String sha1File = file + ".sha1";
			boolean hasMd5 = entries.containsKey(md5File);
			if (!hasMd5) {
				throw new DeploymentFailedException("No MD5 checksum found for file '%s'".formatted(file));
			}
			verifyChecksum(zipFile, file, md5File, Checksum.Algorithm.MD5);
			boolean hasSha1 = entries.containsKey(sha1File);
			if (!hasSha1) {
				throw new DeploymentFailedException("No SHA-1 checksum found for file '%s'".formatted(file));
			}
			verifyChecksum(zipFile, file, sha1File, Checksum.Algorithm.SHA1);
		}
	}

	private void verifyChecksum(ZipFile zipFile, String file, String checksumFile, Checksum.Algorithm algorithm)
			throws IOException {
		Checksum actualChecksum;
		try (InputStream stream = zipFile.getInputStream(zipFile.getEntry(file))) {
			actualChecksum = Checksum.of(algorithm, stream);
		}
		String expectedChecksum;
		try (InputStream stream = zipFile.getInputStream(zipFile.getEntry(checksumFile))) {
			expectedChecksum = StreamUtils.copyToString(stream, StandardCharsets.UTF_8).trim();
		}
		if (!actualChecksum.matches(expectedChecksum)) {
			throw new DeploymentFailedException("%s Checksum does not match for file '%s'".formatted(algorithm, file));
		}
	}

	private void verifySignatures(Map<String, ? extends ZipEntry> entries, String... files) {
		for (String file : files) {
			boolean hasSignature = entries.containsKey(file + ".asc");
			if (!hasSignature) {
				throw new DeploymentFailedException("No signature found for file '%s'".formatted(file));
			}
		}
	}

	private Set<String> findArtifacts(Map<String, ? extends ZipEntry> entries) {
		return entries.keySet()
			.stream()
			.filter((name) -> "pom".equals(StringUtils.getFilenameExtension(name)))
			.map(StringUtils::stripFilenameExtension)
			.collect(Collectors.toSet());
	}

}
