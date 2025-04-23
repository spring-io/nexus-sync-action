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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import io.spring.github.actions.nexussync.file.FileSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BundlerImpl}.
 *
 * @author Moritz Halbritter
 */
class BundlerImplTests {

	@Test
	void shouldCreateBundle(@TempDir Path tempDir) throws IOException {
		FileSet files = createTestFiles(tempDir);
		BundlerImpl bundler = new BundlerImpl();
		Bundle bundle = bundler.createBundle(tempDir, files);
		assertThat(bundle.getFile()).exists();
		assertThat(getBundleEntryNames(bundle)).containsExactlyInAnyOrder("a/a1/aa1.txt", "a/a1/aa2.txt",
				"a/a2/aa1.txt", "b/b1.txt");
	}

	private FileSet createTestFiles(Path tempDir) throws IOException {
		List<Path> files = List.of(tempDir.resolve("a/a1/aa1.txt"), tempDir.resolve("a/a1/aa2.txt"),
				tempDir.resolve("a/a2/aa1.txt"), tempDir.resolve("b/b1.txt"));
		for (Path file : files) {
			createFile(file);
		}
		return FileSet.of(files);
	}

	private void createFile(Path file) throws IOException {
		Files.createDirectories(file.getParent());
		Files.createFile(file);
		Files.writeString(file, "Content of " + file);
	}

	private List<String> getBundleEntryNames(Bundle bundle) throws IOException {
		List<String> result = new ArrayList<>();
		try (ZipFile zipFile = new ZipFile(bundle.getFile().toFile(), StandardCharsets.UTF_8)) {
			zipFile.stream().forEach((entry) -> result.add(entry.getName()));
		}
		return result;
	}

}
