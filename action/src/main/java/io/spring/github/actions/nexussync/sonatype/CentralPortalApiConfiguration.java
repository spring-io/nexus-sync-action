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

import java.time.Clock;

import io.spring.github.actions.nexussync.NexusSyncProperties;
import io.spring.github.actions.nexussync.system.Logger;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for Sonatype's Central Portal API beans.
 *
 * @author Moritz Halbritter
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NexusSyncProperties.class)
class CentralPortalApiConfiguration {

	@Bean
	CentralPortalApi centralPortalApi(NexusSyncProperties properties, Logger logger,
			RestClient.Builder restClientBuilder, Clock clock) {
		NexusSyncProperties.Token token = properties.getToken();
		NexusSyncProperties.Deployment deployment = properties.getDeployment();
		return new CentralPortalApiImpl(logger, properties.getBaseUri(), token.getName(), token.getValue(),
				restClientBuilder, clock, deployment.getTimeout(), deployment.getSleepBetweenRetries());
	}

}
