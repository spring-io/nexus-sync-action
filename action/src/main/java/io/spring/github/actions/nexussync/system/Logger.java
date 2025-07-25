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

/**
 * A logger.
 *
 * @author Moritz Halbritter
 */
public interface Logger {

	/**
	 * Logs the given message.
	 * @param message the message
	 * @param args the arguments
	 */
	void log(String message, Object... args);

	/**
	 * Logs the given error message.
	 * @param message the message
	 * @param args the arguments
	 */
	void error(String message, Object... args);

	/**
	 * Logs the given debug message.
	 * @param message the message
	 * @param args the arguments
	 */
	void debug(String message, Object... args);

	/**
	 * Creates a noop logger.
	 * @return the logger
	 */
	static Logger noop() {
		return new Logger() {
			@Override
			public void log(String message, Object... args) {
			}

			@Override
			public void error(String message, Object... args) {
			}

			@Override
			public void debug(String message, Object... args) {
			}
		};
	}

}
