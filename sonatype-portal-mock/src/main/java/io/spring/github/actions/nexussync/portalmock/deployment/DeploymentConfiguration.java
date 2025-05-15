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
import java.nio.file.Files;
import java.nio.file.Path;

import io.spring.github.actions.nexussync.portalmock.PortalMockProperties;
import io.spring.github.actions.nexussync.portalmock.id.IdFactory;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration for deployments.
 *
 * @author Moritz Halbritter
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
@EnableConfigurationProperties(PortalMockProperties.class)
class DeploymentConfiguration {

	@Bean
	Deployments deployments(PortalMockProperties properties, IdFactory idFactory, AsyncTaskExecutor asyncTaskExecutor,
			BundleValidator deploymentValidator, PublishedDeployments publishedDeployments) throws IOException {
		Path tempDirectory = Files.createTempDirectory("sonatype-portal-mock");
		return new Deployments(tempDirectory, idFactory, asyncTaskExecutor, deploymentValidator, publishedDeployments,
				properties.getDeployment().getDelayBetweenSteps());
	}

}
