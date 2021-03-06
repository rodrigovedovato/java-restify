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
package com.github.ljtfreitas.restify.http.client.call.handler.async;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.CompletionStageAsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.EndpointCallFailureCallback;
import com.github.ljtfreitas.restify.http.client.call.async.EndpointCallSuccessCallback;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class AsyncCallbackEndpointCallHandlerAdapter<T, O> implements AsyncEndpointCallHandlerAdapter<Void, T, O> {

	private final Executor executor;

	public AsyncCallbackEndpointCallHandlerAdapter(Executor executor) {
		this.executor = executor;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		EndpointMethodParameters parameters = endpointMethod.parameters();
		return endpointMethod.runnableAsync()
				&& (!parameters.callbacks(EndpointCallSuccessCallback.class).isEmpty()
						|| !parameters.callbacks(EndpointCallFailureCallback.class).isEmpty());
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return callbackArgumentType(endpointMethod.parameters().callbacks());
	}

	@Override
	public AsyncEndpointCallHandler<Void, O> adaptAsync(EndpointMethod endpointMethod, EndpointCallHandler<T, O> handler) {
		return new AsyncCallbackEndpointCallHandler(endpointMethod.parameters().callbacks(), handler);
	}

	private JavaType callbackArgumentType(Collection<EndpointMethodParameter> callbackMethodParameters) {
		return callbackMethodParameters.stream()
				.filter(p -> EndpointCallSuccessCallback.class.isAssignableFrom(p.javaType().classType()))
					.findFirst()
						.map(p -> p.javaType().parameterized() ? p.javaType().as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class)
							.map(t -> JavaType.of(t))
								.orElseGet(() -> JavaType.of(Void.class));
	}

	private class AsyncCallbackEndpointCallHandler implements AsyncEndpointCallHandler<Void, O> {

		private final Collection<EndpointMethodParameter> callbackMethodParameters;
		private final EndpointCallHandler<T, O> delegate;

		public AsyncCallbackEndpointCallHandler(Collection<EndpointMethodParameter> callbackMethodParameters,
				EndpointCallHandler<T, O> handler) {
			this.callbackMethodParameters = callbackMethodParameters;
			this.delegate = handler;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void handleAsync(AsyncEndpointCall<O> call, Object[] args) {
			CompletionStage<T> stage = call.executeAsync()
					.thenApplyAsync(o -> delegate.handle(() -> o, args), executor);

			AsyncEndpointCall<T> asyncEndpointCall = new CompletionStageAsyncEndpointCall<>(stage, executor);

			EndpointCallSuccessCallback<T> successCallback = callback(EndpointCallSuccessCallback.class, args);
			EndpointCallFailureCallback failureCallback = callback(EndpointCallFailureCallback.class, args);

			asyncEndpointCall.executeAsync(successCallback, failureCallback);

			return null;
		}

		@SuppressWarnings("unchecked")
		private <P> P callback(Class<P> parameterClassType, Object[] args) {
			return callbackMethodParameters.stream()
					.filter(p -> parameterClassType.isAssignableFrom(p.javaType().classType()))
						.findFirst()
							.map(p -> (P) args[p.position()])
								.orElse(null);
		}
	}
}
