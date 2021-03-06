/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.Credentials.basicAuthenticationCredentials;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;

/**
 * @author Rob Winch
 * @since 5.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloWebfluxFnApplicationITests {

	WebTestClient rest;

	@Autowired
	public void setRest(WebTestClient rest) {
		this.rest = rest
				.mutateWith((b, h, c) -> b.filter(ExchangeFilterFunctions.basicAuthentication()));
	}

	@Test
	public void basicWhenNoCredentialsThenUnauthorized() throws Exception {
		this.rest
			.get()
			.uri("/")
			.exchange()
			.expectStatus().isUnauthorized();
	}

	@Test
	public void basicWhenValidCredentialsThenOk() throws Exception {
		this.rest
			.get()
			.uri("/")
			.attributes(userCredentials())
			.exchange()
			.expectStatus().isOk()
			.expectBody().json("{\"message\":\"Hello user!\"}");
	}

	@Test
	public void basicWhenInvalidCredentialsThenUnauthorized() throws Exception {
		this.rest
			.get()
			.uri("/")
			.attributes(invalidCredentials())
			.exchange()
			.expectStatus().isUnauthorized()
			.expectBody().isEmpty();
	}

	private Consumer<Map<String, Object>> userCredentials() {
		return basicAuthenticationCredentials("user", "user");
	}

	private Consumer<Map<String, Object>> invalidCredentials() {
		return basicAuthenticationCredentials("user", "INVALID");
	}
}
