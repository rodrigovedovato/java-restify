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
package com.github.ljtfreitas.restify.http.client.response;

import java.util.function.Function;
import java.util.function.Predicate;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;

public interface EndpointResponse<T> {

	Headers headers();

	StatusCode status();

	T body();

	EndpointResponse<T> recover(Function<EndpointResponseException, EndpointResponse<T>> mapper);

	EndpointResponse<T> recover(Predicate<EndpointResponseException> predicate, Function<EndpointResponseException, EndpointResponse<T>> mapper);

	<E extends EndpointResponseException> EndpointResponse<T> recover(Class<? extends E> predicate, Function<E, EndpointResponse<T>> mapper);

	static <T> EndpointResponse<T> of(StatusCode statusCode, T body) {
		return new SimpleEndpointResponse<T>(statusCode, Headers.empty(), body);
	}

	static <T> EndpointResponse<T> of(StatusCode statusCode, T body, Headers headers) {
		return new SimpleEndpointResponse<>(statusCode, headers, body);
	}

	static <T> EndpointResponse<T> empty(StatusCode statusCode) {
		return empty(statusCode, Headers.empty());
	}

	static <T> EndpointResponse<T> empty(StatusCode statusCode, Headers headers) {
		return new SimpleEndpointResponse<>(statusCode, headers, null);
	}

	static <T> EndpointResponse<T> error(EndpointResponseException e) {
		return new FailureEndpointResponse<>(e);
	}
}