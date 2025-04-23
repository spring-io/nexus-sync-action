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

import java.nio.file.Path;

import io.spring.github.actions.nexussync.file.FileSet;

/**
 * Creates bundles.
 *
 * @author Moritz Halbritter
 */
public interface Bundler {

	/**
	 * Creates a bundle from the given files in the given root directory.
	 * @param root the root directory
	 * @param files the file
	 * @return the created bundle
	 */
	Bundle createBundle(Path root, FileSet files);

}
