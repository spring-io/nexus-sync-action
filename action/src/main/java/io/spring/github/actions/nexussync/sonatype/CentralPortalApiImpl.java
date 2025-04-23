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

package io.spring.github.actions.nexussync.sonatype;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import io.spring.github.actions.nexussync.bundle.Bundle;
import io.spring.github.actions.nexussync.system.Logger;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestClient;

/**
 * Default implementation of {@link CentralPortalApi}. Uses a {@link RestClient} for http
 * communication.
 *
 * @author Moritz Halbritter
 */
// See https://central.sonatype.org/publish/publish-portal-api/
class CentralPortalApiImpl implements CentralPortalApi {

	private static final DataSize MAX_BUNDLE_SIZE = DataSize.ofGigabytes(1);

	private final Logger logger;

	private final RestClient restClient;

	private final Clock clock;

	private final Duration timeout;

	private final Duration sleepBetweenRetries;

	CentralPortalApiImpl(Logger logger, URI baseUri, String tokenName, String token,
			RestClient.Builder restClientBuilder, Clock clock, Duration timeout, Duration sleepBetweenRetries) {
		this.logger = logger;
		this.clock = clock;
		this.timeout = timeout;
		this.sleepBetweenRetries = sleepBetweenRetries;
		this.restClient = restClientBuilder.baseUrl(baseUri)
			.defaultHeader("Accept", "application/json")
			.defaultHeader("User-Agent", "nexus-sync-action")
			.defaultHeader("Authorization", createAuthorizationHeader(tokenName, token))
			.build();
	}

	@Override
	public Deployment upload(Bundle bundle, PublishingType publishingType) {
		DataSize bundleSize = bundle.getSize();
		if (bundleSize.compareTo(MAX_BUNDLE_SIZE) > 0) {
			throw new IllegalStateException("Maximum bundle size is 1 GiB, but the bundle is %s".formatted(bundleSize));
		}
		MultiValueMap<String, Object> body = createBody(bundle);
		String deploymentName = createDeploymentName();
		ResponseEntity<String> response = this.restClient.post()
			.uri("/api/v1/publisher/upload?name={name}&publishingType={publishingType}", deploymentName,
					publishingType.toApi())
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.body(body)
			.retrieve()
			.onStatus((status) -> true, (req, res) -> {
			})
			.toEntity(String.class);
		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new IllegalStateException("Failed to upload bundle '%s', got HTTP %d and body %s"
				.formatted(bundle.getFile(), response.getStatusCode().value(), response.getBody()));
		}
		String deploymentId = response.getBody();
		return new DeploymentImpl(this.logger, this.restClient, deploymentId, publishingType, this.timeout,
				this.sleepBetweenRetries);
	}

	private String createDeploymentName() {
		return "nexus-sync-action-" + this.clock.instant();
	}

	private static MultiValueMap<String, Object> createBody(Bundle bundle) {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		body.add("bundle", new HttpEntity<>(new FileSystemResource(bundle.getFile()), headers));
		return body;
	}

	private static String createAuthorizationHeader(String tokenName, String token) {
		String base64 = Base64.getEncoder()
			.encodeToString("%s:%s".formatted(tokenName, token).getBytes(StandardCharsets.UTF_8));
		return "Bearer " + base64;
	}

	private static class DeploymentImpl implements Deployment {

		private final Logger logger;

		private final RestClient restClient;

		private final String deploymentId;

		private final PublishingType publishingType;

		private final Duration sleepBetweenRetries;

		private final Duration timeout;

		private DeploymentStatusDto finalStatus;

		DeploymentImpl(Logger logger, RestClient restClient, String deploymentId, PublishingType publishingType,
				Duration timeout, Duration sleepBetweenRetries) {
			this.logger = logger;
			this.restClient = restClient;
			this.deploymentId = deploymentId;
			this.publishingType = publishingType;
			this.timeout = timeout;
			this.sleepBetweenRetries = sleepBetweenRetries;
		}

		@Override
		public String getId() {
			return this.deploymentId;
		}

		@Override
		public Status getStatus() {
			Assert.notNull(this.finalStatus, "Call awaitFinalStatus() before calling getStatus()");
			return Status.fromApi(this.finalStatus.deploymentState());
		}

		@Override
		public String getErrors() {
			Assert.notNull(this.finalStatus, "Call awaitFinalStatus() before calling getErrors()");
			if (CollectionUtils.isEmpty(this.finalStatus.errors())) {
				return null;
			}
			return this.finalStatus.errors().toString();
		}

		@Override
		public void awaitFinalStatus() {
			long start = System.nanoTime();
			while (true) {
				Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
				if (elapsed.compareTo(this.timeout) > 0) {
					throw new IllegalStateException(
							"Timeout of '%s' reached while waiting for final status of deployment '%s'"
								.formatted(this.timeout, this.deploymentId));
				}
				DeploymentStatusDto deploymentStatus = fetchDeploymentStatus();
				Status status = Status.fromApi(deploymentStatus.deploymentState());
				this.logger.debug("\tStatus: {}", status);
				if (status.isFinal(this.publishingType)) {
					this.finalStatus = deploymentStatus;
					return;
				}
				sleep();
			}
		}

		@Override
		public void drop() {
			Assert.notNull(this.finalStatus, "Call awaitFinalStatus() before calling drop()");
			Status status = getStatus();
			if (status != Status.VALIDATED && status != Status.FAILED) {
				throw new IllegalStateException(
						"Only validated or failed deployments can be dropped, but status is '%s'".formatted(status));
			}
			this.restClient.delete()
				.uri("/api/v1/publisher/deployment/{deploymentId}", this.deploymentId)
				.retrieve()
				.toBodilessEntity();
		}

		private DeploymentStatusDto fetchDeploymentStatus() {
			return this.restClient.post()
				.uri("/api/v1/publisher/status?id={deploymentId}", this.deploymentId)
				.retrieve()
				.body(DeploymentStatusDto.class);
		}

		private void sleep() {
			try {
				Thread.sleep(this.sleepBetweenRetries.toMillis());
			}
			catch (InterruptedException ex) {
				throw new IllegalStateException("Got interrupted while sleeping", ex);
			}
		}

		private record DeploymentStatusDto(String deploymentState, Map<Object, Object> errors) {
		}

	}

}
