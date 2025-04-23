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

package io.spring.github.actions.nexussync.inttest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.spring.github.actions.nexussync.NexusSync;
import io.spring.github.actions.nexussync.bundle.Bundle;
import io.spring.github.actions.nexussync.sonatype.Deployment;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link NexusSync} that invoke its main method.
 *
 * @author Moritz Halbritter
 */
@ActiveProfiles("inttest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = NexusSyncIntegrationTestsApp.class)
class NexusSyncIntegrationTests {

	private static final String TOKEN_NAME = "token-name";

	private static final String TOKEN = "token";

	@Autowired
	private FakeSonatypePortal sonatypePortal;

	@Test
	void test(@TempDir Path tempDir, @LocalServerPort int port) throws IOException, InterruptedException {
		createFilesToDeploy(tempDir);
		Action action = startAction(tempDir, port);
		Bundle bundle = this.sonatypePortal.awaitUpload();
		this.sonatypePortal.assertCredentials(TOKEN_NAME, TOKEN);
		Thread.sleep(500);
		this.sonatypePortal.setStatus(Deployment.Status.VALIDATING);
		Thread.sleep(500);
		this.sonatypePortal.setStatus(Deployment.Status.VALIDATED);
		Thread.sleep(500);
		this.sonatypePortal.setStatus(Deployment.Status.PUBLISHING);
		Thread.sleep(500);
		this.sonatypePortal.setStatus(Deployment.Status.PUBLISHED);
		action.await();
		this.sonatypePortal.assertCredentials(TOKEN_NAME, TOKEN);
		Path extracted = tempDir.resolve("extracted-bundle");
		extractBundle(bundle, extracted);
		assertBundleContents(extracted);
	}

	private static void assertBundleContents(Path extracted) {
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0-javadoc.jar"))
			.hasContent("some javadoc jar content");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0-javadoc.jar.asc"))
			.hasContent("some javadoc jar gpg content");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0-javadoc.jar.md5"))
			.hasContent("12ef639cd80d6963ec1bfe342c21f50a");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0-javadoc.jar.sha1"))
			.hasContent("5fe47451fe1763eed8291da8b6e5de1372602a3b");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0-sources.jar"))
			.hasContent("some sources jar content");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0-sources.jar.asc"))
			.hasContent("some sources jar gpg content");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0-sources.jar.md5"))
			.hasContent("4f030caee273901f5a8da2d4f04cf0be");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0-sources.jar.sha1"))
			.hasContent("a746378fae015f61d7f26fcbe0c9a64877773cfe");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0.jar"))
			.hasContent("some jar content");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0.jar.asc"))
			.hasContent("some jar gpg content");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0.jar.md5"))
			.hasContent("f8f3dcbb064bbb3aa5f6ed807cb09529");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0.jar.sha1"))
			.hasContent("3d35cf02f8ca549cd185ea4e5644e07203c6efd3");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0.pom"))
			.hasContent("some pom content");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0.pom.asc"))
			.hasContent("some pom gpg content");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0.pom.md5"))
			.hasContent("459ebf19080c8d3b1e19c13b13b1009a");
		assertThat(extracted.resolve("com/example/group/artifact/1.0.0/artifact-1.0.0.pom.sha1"))
			.hasContent("7303b20cf4778d2f0a485024adca8a926ae6bf76");
	}

	private void extractBundle(Bundle bundle, Path dir) throws IOException {
		try (ZipFile zipFile = new ZipFile(bundle.getFile().toFile())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				Path file = dir.resolve(entry.getName()).toAbsolutePath();
				if (!file.startsWith(dir.toAbsolutePath())) {
					throw new IllegalStateException("'%s' is not contained in '%s'".formatted(file, dir));
				}
				Files.createDirectories(file.getParent());
				try (InputStream inputStream = zipFile.getInputStream(entry);
						OutputStream outputStream = Files.newOutputStream(file)) {
					inputStream.transferTo(outputStream);
				}
			}
		}
	}

	private Action startAction(Path tempDir, int port) {
		Action action = new Action("--centralportal.base-uri=http://localhost:%d/".formatted(port),
				"--centralportal.deployment.publishing-type=automatic",
				"--centralportal.token.name=%s".formatted(TOKEN_NAME),
				"--centralportal.token.value=%s".formatted(TOKEN),
				"--centralportal.directory=%s".formatted(tempDir.toAbsolutePath()),
				"--centralportal.deployment.timeout=10s", "--centralportal.deployment.sleep-between-retries=250ms");
		action.start();
		return action;
	}

	private void createFilesToDeploy(Path tempDir) throws IOException {
		Path baseDir = tempDir.resolve("com/example/group/artifact/1.0.0");
		Files.createDirectories(baseDir);
		Files.writeString(baseDir.resolve("artifact-1.0.0-javadoc.jar"), "some javadoc jar content");
		Files.writeString(baseDir.resolve("artifact-1.0.0-javadoc.jar.asc"), "some javadoc jar gpg content");
		Files.writeString(baseDir.resolve("artifact-1.0.0-sources.jar"), "some sources jar content");
		Files.writeString(baseDir.resolve("artifact-1.0.0-sources.jar.asc"), "some sources jar gpg content");
		Files.writeString(baseDir.resolve("artifact-1.0.0.jar"), "some jar content");
		Files.writeString(baseDir.resolve("artifact-1.0.0.jar.asc"), "some jar gpg content");
		Files.writeString(baseDir.resolve("artifact-1.0.0.pom"), "some pom content");
		Files.writeString(baseDir.resolve("artifact-1.0.0.pom.asc"), "some pom gpg content");
	}

	private static class Action {

		private final AtomicReference<Throwable> exception = new AtomicReference<>();

		private final Thread thread;

		Action(String... args) {
			this.thread = new Thread(() -> NexusSync.main(args));
			this.thread.setDaemon(true);
			this.thread.setName("action");
			this.thread.setUncaughtExceptionHandler((t, ex) -> this.exception.set(ex));
		}

		void start() {
			this.thread.start();
		}

		void await() {
			Awaitility.await("action end").atMost(Duration.ofSeconds(30)).until(() -> !this.thread.isAlive());
			Throwable exception = this.exception.get();
			if (exception != null) {
				Assertions.fail("Action crashed", exception);
			}
		}

	}

}
