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

import org.slf4j.LoggerFactory;

/**
 * A {@link Logger} which logs through SLF4J.
 *
 * @author Moritz Halbritter
 */
class Slf4jLogger implements Logger {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Slf4jLogger.class);

	@Override
	public void log(String message, Object... args) {
		LOGGER.info(message, args);
	}

	@Override
	public void error(String message, Object... args) {
		LOGGER.error(message, args);
	}

	@Override
	public void debug(String message, Object... args) {
		LOGGER.debug(message, args);
	}

}
