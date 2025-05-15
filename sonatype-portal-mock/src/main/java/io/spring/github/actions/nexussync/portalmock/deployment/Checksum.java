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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.util.StreamUtils;

/**
 * A checksum.
 *
 * @author Moritz Halbritter
 */
final class Checksum {

	private final Algorithm algorithm;

	private final String checksum;

	private Checksum(Algorithm algorithm, String checksum) {
		this.algorithm = algorithm;
		this.checksum = checksum;
	}

	Algorithm getAlgorithm() {
		return this.algorithm;
	}

	String getChecksum() {
		return this.checksum;
	}

	boolean matches(String expected) {
		return this.checksum.equals(expected);
	}

	static Checksum of(Algorithm algorithm, InputStream inputStream) {
		try (DigestInputStream stream = new DigestInputStream(inputStream,
				MessageDigest.getInstance(algorithm.name()))) {
			StreamUtils.drain(stream);
			byte[] checksum = stream.getMessageDigest().digest();
			return new Checksum(algorithm, HexFormat.of().formatHex(checksum));
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to create checksum", ex);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Unknown checksum algorithm '%s'".formatted(algorithm), ex);
		}
	}

	enum Algorithm {

		MD5, SHA1

	}

}
