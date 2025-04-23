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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.util.unit.DataSize;

/**
 * A deployment bundle.
 *
 * @author Moritz Halbritter
 */
public final class Bundle {

	private final Path file;

	private Bundle(Path file) {
		this.file = file;
	}

	public Path getFile() {
		return this.file;
	}

	public DataSize getSize() {
		try {
			return DataSize.ofBytes(Files.size(this.file));
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to get file size of '%s'".formatted(this.file), ex);
		}
	}

	/**
	 * Creates a new bundle for the given file.
	 * @param file the file
	 * @return the bundle
	 */
	public static Bundle of(Path file) {
		return new Bundle(file);
	}

}
