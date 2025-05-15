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
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileScannerImpl}.
 *
 * @author Moritz Halbritter
 */
class FileScannerImplTests {

	@Test
	void shouldFindFiles(@TempDir Path tempDir) throws IOException {
		createTestFiles(tempDir);
		FileScannerImpl fileScanner = new FileScannerImpl();
		FileSet files = fileScanner.scan(tempDir);
		assertThat(files).containsExactlyInAnyOrder(tempDir.resolve("a/a1/aa1.txt"), tempDir.resolve("a/a1/aa2.txt"),
				tempDir.resolve("a/a2/aa1.txt"), tempDir.resolve("b/b1.txt"));
	}

	private void createTestFiles(Path tempDir) throws IOException {
		createFile(tempDir.resolve("a/a1/aa1.txt"));
		createFile(tempDir.resolve("a/a1/aa2.txt"));
		createFile(tempDir.resolve("a/a2/aa1.txt"));
		createFile(tempDir.resolve("b/b1.txt"));
	}

	private void createFile(Path file) throws IOException {
		Files.createDirectories(file.getParent());
		Files.createFile(file);
	}

}
