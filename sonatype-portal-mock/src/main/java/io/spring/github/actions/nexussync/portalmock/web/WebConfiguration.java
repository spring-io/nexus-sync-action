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

package io.spring.github.actions.nexussync.portalmock.web;

import io.spring.github.actions.nexussync.portalmock.PortalMockProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration for WebMVC / servlet.
 *
 * @author Moritz Halbritter
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PortalMockProperties.class)
class WebConfiguration {

	@Bean
	FilterRegistrationBean<AuthenticationFilter> authenticationFilter(PortalMockProperties properties) {
		FilterRegistrationBean<AuthenticationFilter> registration = new FilterRegistrationBean<>(
				new AuthenticationFilter(properties.getToken().getName(), properties.getToken().getValue()));
		registration.setAsyncSupported(false);
		registration.addUrlPatterns("/api/*");
		registration.setName("authentication");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}

}
