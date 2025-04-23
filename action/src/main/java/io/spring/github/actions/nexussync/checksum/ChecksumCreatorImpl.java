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

package io.spring.github.actions.nexussync.checksum;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.spring.github.actions.nexussync.file.FileSet;
import io.spring.github.actions.nexussync.system.Logger;

import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation for {@link ChecksumCreator}. Creates MD5 and SHA-1 checksums.
 *
 * @author Moritz Halbritter
 */
class ChecksumCreatorImpl implements ChecksumCreator {

	private static final Set<String> IGNORED_FILE_EXTENSIONS = Set.of("asc", "md5", "sha1");

	private final Logger logger;

	private final ChecksumPolicy checksumPolicy;

	ChecksumCreatorImpl(Logger logger, ChecksumPolicy checksumPolicy) {
		this.logger = logger;
		this.checksumPolicy = checksumPolicy;
	}

	@Override
	public FileSet createChecksums(FileSet files) {
		List<Path> checksumFiles = new ArrayList<>();
		for (Path path : files) {
			if (matches(path)) {
				checksumFiles.add(createChecksumFile("md5", path));
				checksumFiles.add(createChecksumFile("sha1", path));
			}
		}
		return FileSet.of(checksumFiles);
	}

	private Path createChecksumFile(String algorithm, Path path) {
		Path checksumFile = Path.of(path.toString() + "." + algorithm);
		if (Files.exists(checksumFile)) {
			this.checksumPolicy.checksumExists(checksumFile);
		}
		this.logger.debug("Creating {} checksum for {}", algorithm, path);
		String checksum = createChecksum(algorithm, path);
		try {
			Files.writeString(checksumFile, checksum);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to write checksum to file '%s'".formatted(checksumFile), ex);
		}
		return checksumFile;
	}

	private String createChecksum(String algorithm, Path path) {
		try (DigestInputStream stream = new DigestInputStream(Files.newInputStream(path),
				MessageDigest.getInstance(algorithm))) {
			StreamUtils.drain(stream);
			byte[] checksum = stream.getMessageDigest().digest();
			return HexFormat.of().formatHex(checksum);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to create checksum for file '%s'".formatted(path), ex);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Unknown checksum algorithm '%s'".formatted(algorithm), ex);
		}
	}

	private boolean matches(Path file) {
		String extension = StringUtils.getFilenameExtension(file.toString());
		if (extension == null) {
			return true;
		}
		return !IGNORED_FILE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
	}

	enum ChecksumPolicy {

		FAIL_ON_EXISTING {
			@Override
			void checksumExists(Path checksumFile) {
				throw new IllegalStateException(
						"Checksum file '%s' already exists'".formatted(checksumFile.toAbsolutePath()));
			}
		},
		OVERWRITE_EXISTING {
			@Override
			void checksumExists(Path checksumFile) {
				// noop
			}
		};

		abstract void checksumExists(Path checksumFile);

	}

}
