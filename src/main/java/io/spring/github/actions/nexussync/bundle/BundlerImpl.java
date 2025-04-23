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

package io.spring.github.actions.nexussync.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.spring.github.actions.nexussync.file.FileSet;

import org.springframework.stereotype.Component;

/**
 * Default implementation for {@link Bundler}. Creates compressed zip bundles.
 *
 * @author Moritz Halbritter
 */
@Component
class BundlerImpl implements Bundler {

	@Override
	public Bundle createBundle(Path root, FileSet files) {
		try {
			Path bundle = Files.createTempFile("bundle", ".zip");
			try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(bundle), StandardCharsets.UTF_8)) {
				zip.setMethod(ZipOutputStream.DEFLATED);
				for (Path file : files) {
					ZipEntry entry = createZipEntry(root, file);
					zip.putNextEntry(entry);
					copyFileContents(file, zip);
					zip.closeEntry();
				}
			}
			return Bundle.of(bundle);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to create bundle", ex);
		}
	}

	private void copyFileContents(Path file, OutputStream outputStream) throws IOException {
		try (InputStream inputStream = Files.newInputStream(file)) {
			inputStream.transferTo(outputStream);
		}
	}

	private ZipEntry createZipEntry(Path root, Path file) throws IOException {
		String name = root.relativize(file).toString();
		return new ZipEntry(name);
	}

}
