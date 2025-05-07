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

package io.spring.github.actions.nexussync.portalmock;

import java.time.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Global configuration properties.
 *
 * @author Moritz Halbritter
 */
@ConfigurationProperties(prefix = "portalmock")
@Validated
public class PortalMockProperties {

	@Valid
	private final Token token = new Token();

	@Valid
	private final Deployment deployment = new Deployment();

	public Token getToken() {
		return this.token;
	}

	public Deployment getDeployment() {
		return this.deployment;
	}

	@Validated
	public static class Deployment {

		private Duration delayBetweenSteps = Duration.ZERO;

		public Duration getDelayBetweenSteps() {
			return this.delayBetweenSteps;
		}

		public void setDelayBetweenSteps(Duration delayBetweenSteps) {
			this.delayBetweenSteps = delayBetweenSteps;
		}

	}

	@Validated
	public static class Token {

		@NotBlank
		private String name;

		@NotBlank
		private String value;

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

}
