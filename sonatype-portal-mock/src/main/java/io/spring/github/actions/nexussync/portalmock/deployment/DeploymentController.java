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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link RestController} which simulates some parts of the Sonatype central portal API.
 *
 * @author Moritz Halbritter
 */
@RestController
class DeploymentController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentController.class);

	private final Deployments deployments;

	DeploymentController(Deployments deployments) {
		this.deployments = deployments;
	}

	@PostMapping(path = "/api/v1/publisher/upload")
	ResponseEntity<String> upload(@RequestParam(value = "name", defaultValue = "deployment") String name,
			@RequestParam(value = "publishingType", defaultValue = "AUTOMATIC") String publishingType,
			@RequestParam("bundle") MultipartFile file) {
		LOGGER.info("Received upload request for name {} and publishingType {}, {} bytes", name, publishingType,
				file.getSize());
		String id = this.deployments.create(name, Deployment.PublishingType.parse(publishingType), file.getResource());
		LOGGER.info("Created deployment {}", id);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PostMapping(path = "/api/v1/publisher/status")
	ResponseEntity<?> status(@RequestParam("id") String id) {
		LOGGER.debug("Received status request for id {}", id);
		Deployment deployment = this.deployments.find(id);
		if (deployment == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deployment '%s' not found".formatted(id));
		}
		Map<String, String> errors = (deployment.getErrors() != null) ? Map.of("error", deployment.getErrors()) : null;
		StatusResponse response = new StatusResponse(deployment.getId(), deployment.getName(),
				deployment.getStatus().name(), deployment.getPurls(), errors);
		return ResponseEntity.ok(response);
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private record StatusResponse(String deploymentId, String deploymentName, String deploymentState,
			List<String> purls, @Nullable Map<String, String> errors) {
	}

}
