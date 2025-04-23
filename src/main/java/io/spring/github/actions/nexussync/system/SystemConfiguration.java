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

package io.spring.github.actions.nexussync.system;

import java.time.Clock;

import io.spring.github.actions.nexussync.NexusSyncProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for system beans.
 *
 * @author Moritz Halbritter
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NexusSyncProperties.class)
class SystemConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	Logger logger() {
		if (runsOnGithubActions()) {
			boolean debugEnabled = Boolean.parseBoolean(System.getenv("ACTIONS_STEP_DEBUG"));
			return new GithubActionsLogger(debugEnabled);
		}
		return new Slf4jLogger();
	}

	private boolean runsOnGithubActions() {
		return Boolean.parseBoolean(System.getenv("GITHUB_ACTIONS"));
	}

}
