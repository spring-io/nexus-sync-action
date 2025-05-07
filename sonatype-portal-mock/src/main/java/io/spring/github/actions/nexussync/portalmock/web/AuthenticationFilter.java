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

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@link jakarta.servlet.Filter} which checks if a request contains a valid
 * 'Authorization: Bearer' header.
 *
 * @author Moritz Halbritter
 */
class AuthenticationFilter extends OncePerRequestFilter {

	private final String tokenName;

	private final String token;

	AuthenticationFilter(String tokenName, String token) {
		this.tokenName = tokenName;
		this.token = token;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Header 'Authorization' not found");
			return;
		}
		if (!header.startsWith("Bearer ")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Header 'Authorization' doesn't start with 'Bearer'");
			return;
		}
		String token = header.substring("Bearer ".length());
		String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
		if (!decoded.equals(this.tokenName + ":" + this.token)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().println("Incorrect token");
			return;
		}
		filterChain.doFilter(request, response);
	}

}
