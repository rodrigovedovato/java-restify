/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.request.vertx;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.Timeout;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

class VertxHttpClientRequest implements AsyncHttpClientRequest {

	private final WebClient webClient;
	private final EndpointRequest source;
	private final Headers headers;
	private final Charset charset;
	private final BufferHttpRequestBody body;

	VertxHttpClientRequest(WebClient webClient, EndpointRequest source, Charset charset) {
		this(webClient, source, new Headers(source.headers()), charset, new BufferHttpRequestBody());
	}

	private VertxHttpClientRequest(WebClient webClient, EndpointRequest source, Headers headers, Charset charset,
			BufferHttpRequestBody body) {
		this.webClient = webClient;
		this.source = source;
		this.headers = headers;
		this.charset = charset;
		this.body = body;
	}

	@Override
	public URI uri() {
		return source.endpoint();
	}

	@Override
	public String method() {
		return source.method();
	}

	@Override
	public HttpRequestBody body() {
		return body;
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public HttpRequestMessage replace(Header header) {
		return new VertxHttpClientRequest(webClient, source, headers.replace(header), charset, body);
	}

	@Override
	public Headers headers() {
		return headers;
	}

	@Override
	public CompletionStage<HttpClientResponse> executeAsync() throws HttpClientException {
		CompletableFuture<HttpClientResponse> responseAsFuture = new CompletableFuture<>();

		HttpRequest<Buffer> request = buildRequest();

		request
			.sendBuffer(body.buffer(), r -> {
				if (r.succeeded()) {
					responseAsFuture.complete(read(r.result()));

				} else {
					responseAsFuture.completeExceptionally(wrap(r.cause()));
				}
			});

		return responseAsFuture;
	}

	private HttpRequest<Buffer> buildRequest() {
		HttpRequest<Buffer> request = webClient
			.requestAbs(HttpMethod.valueOf(source.method()), source.endpoint().toString());

		source.headers()
			.forEach(header -> request.headers().add(header.name(), header.value()));

		source.metadata().get(Timeout.class).ifPresent(timeout -> {
			request.timeout((int) timeout.read());
		});

		return request;
	}

	private VertxHttpClientResponse read(HttpResponse<Buffer> response) {
		return VertxHttpClientResponse.read(response, this);
	}

	private HttpClientException wrap(Throwable cause) {
		return new HttpClientException("I/O error on HTTP request: [" + source.method() + " " +
				source.endpoint() + "]", cause);
	}
}
