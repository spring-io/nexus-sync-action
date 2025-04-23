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

package io.spring.github.actions.nexussync;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application properties.
 *
 * @author Moritz Halbritter
 */
@ConfigurationProperties(prefix = "centralportal")
@Validated
public class NexusSyncProperties {

	@NotNull
	private URI baseUri = URI.create("https://central.sonatype.com");

	@Valid
	private final Token token = new Token();

	@Valid
	private final Checksum checksum = new Checksum();

	@Valid
	private final Deployment deployment = new Deployment();

	@NotBlank
	private String directory = "nexus";

	public URI getBaseUri() {
		return this.baseUri;
	}

	public void setBaseUri(URI baseUri) {
		this.baseUri = baseUri;
	}

	public Token getToken() {
		return this.token;
	}

	public String getDirectory() {
		return this.directory;
	}

	public Path getDirectoryAsPath() {
		return Path.of(this.directory).toAbsolutePath();
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public Checksum getChecksum() {
		return this.checksum;
	}

	public Deployment getDeployment() {
		return this.deployment;
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

	@Validated
	public static class Checksum {

		private boolean failOnExistingChecksums = true;

		public boolean isFailOnExistingChecksums() {
			return this.failOnExistingChecksums;
		}

		public void setFailOnExistingChecksums(boolean failOnExistingChecksums) {
			this.failOnExistingChecksums = failOnExistingChecksums;
		}

	}

	@Validated
	public static class Deployment {

		@NotNull
		private PublishingType publishingType = PublishingType.AUTOMATIC;

		private boolean dropOnFailure = true;

		private Duration timeout = Duration.ofSeconds(600);

		private Duration sleepBetweenRetries = Duration.ofSeconds(10);

		public PublishingType getPublishingType() {
			return this.publishingType;
		}

		public void setPublishingType(PublishingType publishingType) {
			this.publishingType = publishingType;
		}

		public boolean isDropOnFailure() {
			return this.dropOnFailure;
		}

		public void setDropOnFailure(boolean dropOnFailure) {
			this.dropOnFailure = dropOnFailure;
		}

		public Duration getTimeout() {
			return this.timeout;
		}

		public void setTimeout(Duration timeout) {
			this.timeout = timeout;
		}

		public Duration getSleepBetweenRetries() {
			return this.sleepBetweenRetries;
		}

		public void setSleepBetweenRetries(Duration sleepBetweenRetries) {
			this.sleepBetweenRetries = sleepBetweenRetries;
		}

	}

	public enum PublishingType {

		/**
		 * Automatic publishing.
		 */
		AUTOMATIC,

		/**
		 * Manual step after publishing required.
		 */
		USER_MANAGED

	}

}
