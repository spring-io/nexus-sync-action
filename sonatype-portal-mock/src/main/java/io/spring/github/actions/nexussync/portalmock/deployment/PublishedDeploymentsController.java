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

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link RestController} to inspect and download published deployments.
 *
 * @author Moritz Halbritter
 */
@RestController
@RequestMapping(path = "/debug/published-deployments", produces = MediaType.APPLICATION_JSON_VALUE)
class PublishedDeploymentsController {

	private final PublishedDeployments publishedDeployments;

	PublishedDeploymentsController(PublishedDeployments publishedDeployments) {
		this.publishedDeployments = publishedDeployments;
	}

	@GetMapping
	List<PublishedDeploymentDto> listAll() {
		return this.publishedDeployments.list()
			.stream()
			.map((e) -> new PublishedDeploymentDto(e.id(), e.bundle().toString()))
			.toList();
	}

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	ResponseEntity<Resource> get(@PathVariable("id") String id) {
		Path bundle = this.publishedDeployments.get(id);
		if (bundle == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new FileSystemResource(bundle));
	}

	record PublishedDeploymentDto(String id, String file) {
	}

}
