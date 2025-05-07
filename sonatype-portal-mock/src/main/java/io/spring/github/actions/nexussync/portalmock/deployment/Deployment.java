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

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.springframework.lang.Nullable;

/**
 * A deployment.
 *
 * @author Moritz Halbritter
 */
class Deployment {

	private final String id;

	private final PublishingType publishingType;

	private final String name;

	private final Status status;

	private final List<String> purls;

	private final Path bundle;

	private final @Nullable String errors;

	Deployment(String id, PublishingType publishingType, String name, Path bundle) {
		this(id, publishingType, name, Status.PENDING, Collections.emptyList(), bundle, null);
	}

	Deployment(String id, PublishingType publishingType, String name, Status status, List<String> purls, Path bundle,
			@Nullable String errors) {
		this.id = id;
		this.publishingType = publishingType;
		this.name = name;
		this.status = status;
		this.purls = purls;
		this.bundle = bundle;
		this.errors = errors;
	}

	String getId() {
		return this.id;
	}

	String getName() {
		return this.name;
	}

	Status getStatus() {
		return this.status;
	}

	List<String> getPurls() {
		return this.purls;
	}

	Path getBundle() {
		return this.bundle;
	}

	PublishingType getPublishingType() {
		return this.publishingType;
	}

	@Nullable
	String getErrors() {
		return this.errors;
	}

	Deployment withStatus(Status status) {
		return new Deployment(this.id, this.publishingType, this.name, status, this.purls, this.bundle, this.errors);
	}

	Deployment withErrors(String errors) {
		return new Deployment(this.id, this.publishingType, this.name, this.status, this.purls, this.bundle, errors);
	}

	enum PublishingType {

		AUTOMATIC, USER_MANAGED;

		static PublishingType parse(String value) {
			return switch (value) {
				case "AUTOMATIC" -> AUTOMATIC;
				case "USER_MANAGED" -> USER_MANAGED;
				default -> throw new IllegalArgumentException("Unknown publishing type: " + value);
			};
		}

	}

	enum Status {

		/**
		 * Sonatype is thinking.
		 */
		PENDING,
		/**
		 * Deployment is validating.
		 */
		VALIDATING,
		/**
		 * Deployment has been validated. This is a final state if user-managed deployment
		 * has been used.
		 */
		VALIDATED,
		/**
		 * Deployment is publishing.
		 */
		PUBLISHING,
		/**
		 * Deployment has been published. This is a final state if automatic deployment
		 * has been used.
		 */
		PUBLISHED,
		/**
		 * Deployment has failed. This is a final state.
		 */
		FAILED

	}

}
