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

package io.spring.github.actions.nexussync.portalmock.deployment;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Manages published deployments.
 *
 * @author Moritz Halbritter
 */
@Component
class PublishedDeployments {

	private final Map<String, Path> deployments = new ConcurrentHashMap<>();

	void add(String id, Path bundle) {
		this.deployments.put(id, bundle);
	}

	List<PublishedDeployment> list() {
		return this.deployments.entrySet()
			.stream()
			.map((e) -> new PublishedDeployment(e.getKey(), e.getValue().toAbsolutePath()))
			.toList();
	}

	@Nullable
	Path get(String id) {
		return this.deployments.get(id);
	}

	record PublishedDeployment(String id, Path bundle) {
	}

}
