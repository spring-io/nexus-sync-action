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

package io.spring.github.actions.nexussync.file;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

/**
 * Default implementation for {@link FileScanner}.
 *
 * @author Moritz Halbritter
 */
@Component
class FileScannerImpl implements FileScanner {

	@Override
	public FileSet scan(Path root) {
		try {
			try (Stream<Path> stream = Files.walk(root)) {
				List<Path> files = stream.filter(this::matches).toList();
				return FileSet.of(files);
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to scan files", ex);
		}
	}

	private boolean matches(Path path) {
		return Files.isRegularFile(path);
	}

}
