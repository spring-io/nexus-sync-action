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

import org.springframework.lang.Nullable;

/**
 * A deployment.
 *
 * @author Moritz Halbritter
 */
public interface Deployment {

	/**
	 * Returns the id of the deployment.
	 * @return the id
	 */
	String getId();

	/**
	 * Returns the status. Will throw an exception if called before
	 * {@link #awaitFinalStatus()} has been called.
	 * @return the status
	 */
	Status getStatus();

	/**
	 * Returns the errors. Will throw an exception if called before
	 * {@link #awaitFinalStatus()} has been called.
	 * @return the errors
	 */
	@Nullable
	String getErrors();

	/**
	 * Awaits the final status of the deployment.
	 */
	void awaitFinalStatus();

	/**
	 * Drops the deployment. Will throw an exception if called before
	 * {@link #awaitFinalStatus()} has been called.
	 */
	void drop();

	/**
	 * Deployment status.
	 */
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
		FAILED;

		/**
		 * Whether this status is final for the given publishing type.
		 * @param publishingType the publishing type
		 * @return whether this status is final
		 */
		public boolean isFinal(PublishingType publishingType) {
			return switch (this) {
				case PENDING, VALIDATING, PUBLISHING -> false;
				case VALIDATED -> publishingType == PublishingType.USER_MANAGED;
				case PUBLISHED -> publishingType == PublishingType.AUTOMATIC;
				case FAILED -> true;
			};
		}

		/**
		 * Converts the given status string from the api into a {@link Status}.
		 * @param value the api value
		 * @return the status
		 */
		public static Status fromApi(String value) {
			return switch (value) {
				case "PENDING" -> PENDING;
				case "VALIDATING" -> VALIDATING;
				case "VALIDATED" -> VALIDATED;
				case "PUBLISHING" -> PUBLISHING;
				case "PUBLISHED" -> PUBLISHED;
				case "FAILED" -> FAILED;
				default -> throw new IllegalStateException("Unexpected value '%s'".formatted(value));
			};
		}

	}

}
