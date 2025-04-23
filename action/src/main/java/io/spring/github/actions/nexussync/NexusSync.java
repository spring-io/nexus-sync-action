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

import io.spring.github.actions.nexussync.deploy.Deployer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main Application entry point.
 *
 * @author Moritz Halbritter
 */
@SpringBootApplication(proxyBeanMethods = false)
public class NexusSync {

	public static void main(String... args) {
		Deployer.Result result;
		try (ConfigurableApplicationContext app = SpringApplication.run(NexusSync.class, args)) {
			result = app.getBean(Deployer.class).deploy();
		}
		int status = switch (result) {
			case SUCCESS -> 0;
			case FAILURE -> 1;
		};
		System.exit(status);
	}

}
