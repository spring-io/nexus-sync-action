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

package io.spring.github.actions.nexussync.inttest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.spring.github.actions.nexussync.bundle.Bundle;
import io.spring.github.actions.nexussync.sonatype.Deployment;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fake Sonatype portal.
 *
 * @author Moritz Halbritter
 */
@RestController
class FakeSonatypePortal {

	private static final Logger LOGGER = LoggerFactory.getLogger(FakeSonatypePortal.class);

	private static final String DEPLOYMENT_ID = "1";

	private final AtomicReference<Deployment.Status> status = new AtomicReference<>(Deployment.Status.PENDING);

	private final AtomicReference<Path> bundle = new AtomicReference<>();

	private final AtomicReference<String> authorizationHeader = new AtomicReference<>();

	@PostMapping(path = "/api/v1/publisher/upload")
	String upload(@RequestParam("name") String name, @RequestParam("publishingType") String publishingType,
			@RequestParam("bundle") MultipartFile file, @RequestHeader("Authorization") String authorization)
			throws IOException {
		LOGGER.info("Received upload request for name {} and publishingType {}, {} bytes", name, publishingType,
				file.getSize());
		this.authorizationHeader.set(authorization);
		Path bundle = saveToFile(file);
		LOGGER.debug("Saved bundle to {}", this.bundle);
		this.bundle.set(bundle);
		return DEPLOYMENT_ID;
	}

	Bundle awaitUpload() {
		Awaitility.await("upload").atMost(Duration.ofSeconds(10)).untilAtomic(this.bundle, Matchers.notNullValue());
		return Bundle.of(this.bundle.get());
	}

	@PostMapping(path = "/api/v1/publisher/status")
	StatusResponse status(@RequestParam("id") String id, @RequestHeader("Authorization") String authorization) {
		LOGGER.debug("Received status request for id {}", id);
		this.authorizationHeader.set(authorization);
		if (!DEPLOYMENT_ID.equals(id)) {
			throw new IllegalStateException(
					"Expected status request for id '%s', got '%s'".formatted(DEPLOYMENT_ID, id));
		}
		return new StatusResponse(DEPLOYMENT_ID, "dummy", this.status.get().name(), Collections.emptyList());
	}

	void setStatus(Deployment.Status status) {
		this.status.set(status);
	}

	void assertCredentials(String tokenName, String token) {
		String authorization = this.authorizationHeader.get();
		assertThat(authorization).isNotNull();
		assertThat(authorization).startsWith("Bearer ");
		String base64 = authorization.substring("Bearer ".length());
		String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
		assertThat(decoded).isEqualTo(tokenName + ":" + token);
	}

	private Path saveToFile(MultipartFile file) throws IOException {
		Path tempFile = Files.createTempFile("bundle", ".zip");
		try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
			file.getInputStream().transferTo(outputStream);
		}
		return tempFile;
	}

	private record StatusResponse(String deploymentId, String deploymentName, String deploymentState,
			List<String> purls) {
	}

}
