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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@link jakarta.servlet.Filter} which checks if a request contains a valid
 * 'Authorization: Bearer' header.
 *
 * @author Moritz Halbritter
 */
class AuthenticationFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

	private final String tokenName;

	private final String token;

	AuthenticationFilter(String tokenName, String token) {
		this.tokenName = tokenName;
		this.token = token;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null) {
			LOGGER.debug("No {} header found", HttpHeaders.AUTHORIZATION);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Header 'Authorization' not found");
			return;
		}
		if (!header.startsWith("Bearer ")) {
			LOGGER.debug("Invalid Authorization header found. Header is '{}'", header);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Header 'Authorization' doesn't start with 'Bearer'");
			return;
		}
		String token = header.substring("Bearer ".length());
		LOGGER.debug("Found token '{}'", token);
		String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
		LOGGER.debug("Found decoded token '{}'", decoded);
		String expectedToken = this.tokenName + ":" + this.token;
		if (!decoded.equals(expectedToken)) {
			LOGGER.debug("Provided token '{}' does not match expected token '{}'", decoded, expectedToken);
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().println("Incorrect token");
			return;
		}
		LOGGER.debug("Valid token found, proceeding");
		filterChain.doFilter(request, response);
	}

}
