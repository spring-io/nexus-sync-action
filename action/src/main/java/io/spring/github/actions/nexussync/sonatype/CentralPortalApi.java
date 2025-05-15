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

import io.spring.github.actions.nexussync.bundle.Bundle;

/**
 * Client for Sonatype's Central Portal API.
 *
 * @author Moritz Halbritter
 */
public interface CentralPortalApi {

	/**
	 * Uploads the given bundle using the given publishing type.
	 * @param bundle the bundle to upload
	 * @param publishingType the publishing type
	 * @return the deployment
	 */
	Deployment upload(Bundle bundle, PublishingType publishingType);

}
