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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.spring.github.actions.nexussync.portalmock.id.IdFactory;

import org.springframework.core.io.Resource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.lang.Nullable;

/**
 * Manages deployments.
 *
 * @author Moritz Halbritter
 */
class Deployments {

	private final Path baseDir;

	private final IdFactory idFactory;

	private final Map<String, Deployment> deployments = new ConcurrentHashMap<>();

	private final AsyncTaskExecutor asyncTaskExecutor;

	private final BundleValidator bundleValidator;

	private final PublishedDeployments publishedDeployments;

	private final Duration delayBetweenSteps;

	Deployments(Path baseDir, IdFactory idFactory, AsyncTaskExecutor asyncTaskExecutor, BundleValidator bundleValidator,
			PublishedDeployments publishedDeployments, Duration delayBetweenSteps) {
		this.baseDir = baseDir.toAbsolutePath();
		this.idFactory = idFactory;
		this.asyncTaskExecutor = asyncTaskExecutor;
		this.bundleValidator = bundleValidator;
		this.publishedDeployments = publishedDeployments;
		this.delayBetweenSteps = delayBetweenSteps;
	}

	String create(String name, Deployment.PublishingType publishingType, Resource resource) {
		String id = this.idFactory.generate();
		Path bundle = getDeploymentFile(id);
		store(resource, bundle);
		this.deployments.put(id, new Deployment(id, publishingType, name, bundle));
		processDeployment(id, bundle, publishingType);
		return id;
	}

	private void processDeployment(String id, Path bundle, Deployment.PublishingType publishingType) {
		this.asyncTaskExecutor.submit(() -> {
			try {
				sleep(this.delayBetweenSteps);
				setDeploymentStatus(id, Deployment.Status.VALIDATING);
				this.bundleValidator.validate(bundle);
				sleep(this.delayBetweenSteps);
				setDeploymentStatus(id, Deployment.Status.VALIDATED);
				if (publishingType == Deployment.PublishingType.USER_MANAGED) {
					return;
				}
				sleep(this.delayBetweenSteps);
				setDeploymentStatus(id, Deployment.Status.PUBLISHING);
				sleep(this.delayBetweenSteps);
				this.publishedDeployments.add(id, bundle);
				setDeploymentStatus(id, Deployment.Status.PUBLISHED);
			}
			catch (Exception ex) {
				setDeploymentStatus(id, Deployment.Status.FAILED, ex.getMessage());
			}
		});
	}

	private void sleep(Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
		}
		catch (InterruptedException ex) {
			throw new RuntimeException("Got interrupted while sleeping", ex);
		}
	}

	private void setDeploymentStatus(String id, Deployment.Status status) {
		setDeploymentStatus(id, status, null);
	}

	private void setDeploymentStatus(String id, Deployment.Status status, @Nullable String errors) {
		this.deployments.computeIfPresent(id, (k, deployment) -> deployment.withStatus(status).withErrors(errors));
	}

	@Nullable
	Deployment find(String id) {
		return this.deployments.get(id);
	}

	private Path getDeploymentFile(String id) {
		return this.baseDir.resolve(id).resolve("bundle.zip");
	}

	private void store(Resource resource, Path bundle) {
		try {
			Files.createDirectories(bundle.getParent());
			try (InputStream inputStream = resource.getInputStream();
					OutputStream outputStream = Files.newOutputStream(bundle)) {
				inputStream.transferTo(outputStream);
			}
		}
		catch (IOException ex) {
			throw new RuntimeException("Failed to store bundle to '%s'".formatted(bundle), ex);
		}
	}

}
