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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.spring.github.actions.nexussync.file.FileSet;
import io.spring.github.actions.nexussync.system.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ChecksumCreatorImpl}.
 *
 * @author Moritz Halbritter
 */
class ChecksumCreatorImplTests {

	@Test
	void shouldCreateChecksumFiles(@TempDir Path tempDir) throws IOException {
		FileSet files = createTestFiles(tempDir);
		ChecksumCreatorImpl checksumCreator = new ChecksumCreatorImpl(Logger.noop(),
				ChecksumCreatorImpl.ChecksumPolicy.FAIL_ON_EXISTING);
		checksumCreator.createChecksums(files);
		assertThat(tempDir.resolve("file1.txt.md5")).exists()
			.content(StandardCharsets.UTF_8)
			.isEqualTo("fedad3341c60a233922c47cbd6485643");
		assertThat(tempDir.resolve("file1.txt.sha1")).exists()
			.content(StandardCharsets.UTF_8)
			.isEqualTo("49031614efd805062681b21a8b00884aebc8d953");
		assertThat(tempDir.resolve("file2.txt.md5")).exists()
			.content(StandardCharsets.UTF_8)
			.isEqualTo("0601e2ce163f3c76ea6b5da3662248a5");
		assertThat(tempDir.resolve("file2.txt.sha1")).exists()
			.content(StandardCharsets.UTF_8)
			.isEqualTo("2b86942270efab26aa5a0582dc359b4fdec55105");
	}

	private FileSet createTestFiles(Path tempDir) throws IOException {
		Path file1 = tempDir.resolve("file1.txt");
		Files.writeString(file1, "Content of file1.txt");
		Path file2 = tempDir.resolve("file2.txt");
		Files.writeString(file2, "Content of file2.txt");
		return FileSet.of(List.of(file1, file2));
	}

}
