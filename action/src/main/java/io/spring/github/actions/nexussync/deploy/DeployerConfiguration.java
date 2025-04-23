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

package io.spring.github.actions.nexussync.deploy;

import io.spring.github.actions.nexussync.NexusSyncProperties;
import io.spring.github.actions.nexussync.bundle.Bundler;
import io.spring.github.actions.nexussync.checksum.ChecksumCreator;
import io.spring.github.actions.nexussync.file.FileScanner;
import io.spring.github.actions.nexussync.sonatype.CentralPortalApi;
import io.spring.github.actions.nexussync.sonatype.PublishingType;
import io.spring.github.actions.nexussync.system.Logger;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for deployment beans.
 *
 * @author Moritz Halbritter
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NexusSyncProperties.class)
class DeployerConfiguration {

	@Bean
	Deployer deployer(NexusSyncProperties properties, Logger logger, FileScanner fileScanner,
			ChecksumCreator checksumCreator, Bundler bundler, CentralPortalApi centralPortalApi) {
		NexusSyncProperties.Deployment deployment = properties.getDeployment();
		return new Deployer(logger, properties.getDirectoryAsPath(), getPublishingType(deployment), fileScanner,
				checksumCreator, bundler, centralPortalApi, deployment.isDropOnFailure());
	}

	private PublishingType getPublishingType(NexusSyncProperties.Deployment properties) {
		return switch (properties.getPublishingType()) {
			case AUTOMATIC -> PublishingType.AUTOMATIC;
			case USER_MANAGED -> PublishingType.USER_MANAGED;
		};
	}

}
