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

/**
 * Publishing type.
 *
 * @author Moritz Halbritter
 */
public enum PublishingType {

	/**
	 * Automatic publishing.
	 */
	AUTOMATIC,
	/**
	 * Publishing, which requires a manual step at the end.
	 */
	USER_MANAGED;

	/**
	 * Converts this publishing API to the value of the query parameter in the API.
	 * @return the query parameter value
	 */
	public String toApi() {
		return switch (this) {
			case AUTOMATIC -> "AUTOMATIC";
			case USER_MANAGED -> "USER_MANAGED";
		};
	}

}
