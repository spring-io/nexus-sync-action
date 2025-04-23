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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A collection of files.
 *
 * @author Moritz Halbritter
 */
public final class FileSet implements Iterable<Path> {

	private final Set<Path> files;

	private FileSet(Set<Path> files) {
		this.files = files;
	}

	@Override
	public Iterator<Path> iterator() {
		return this.files.iterator();
	}

	/**
	 * Whether this collection is empty.
	 * @return whether this collection is empty
	 */
	public boolean isEmpty() {
		return this.files.isEmpty();
	}

	/**
	 * Returns the size of the collection.
	 * @return the size of the collection
	 */
	public int size() {
		return this.files.size();
	}

	/**
	 * Adds the given files to this collection and returns a new collection.
	 * @param other the files to add
	 * @return a collection containing files contained in this and the other collection
	 */
	public FileSet plus(FileSet other) {
		Set<Path> files = new HashSet<>(this.files);
		files.addAll(other.files);
		return new FileSet(files);
	}

	/**
	 * Creates an empty collection.
	 * @return the empty collection
	 */
	public static FileSet empty() {
		return new FileSet(Collections.emptySet());
	}

	/**
	 * Creates a collection of the given files.
	 * @param files the files
	 * @return the collection containing the given files
	 */
	public static FileSet of(Collection<Path> files) {
		return new FileSet(files.stream().map(Path::toAbsolutePath).collect(Collectors.toSet()));
	}

}
