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

import java.nio.file.Path;

/**
 * Scans for files.
 *
 * @author Moritz Halbritter
 */
public interface FileScanner {

	/**
	 * Scans the given root folder and all subfolders for files.
	 * @param root the root folder
	 * @return the found files
	 */
	FileSet scan(Path root);

}
