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
package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.Fallback;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.FallbackProvider;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.WithFallbackProvider;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;

public class HystrixCommandEndpointCallHandlerAdapter<T, O> implements EndpointCallHandlerAdapter<HystrixCommand<T>, T, O> {

	private final HystrixCommand.Setter properties;
	private final FallbackProvider fallback;
	private final HystrixCommandMetadataFactory hystrixCommandMetadataFactory;

	public HystrixCommandEndpointCallHandlerAdapter() {
		this(null, (FallbackProvider) null, null);
	}

	public HystrixCommandEndpointCallHandlerAdapter(HystrixCommand.Setter properties) {
		this(properties, (FallbackProvider) null, null);
	}

	public HystrixCommandEndpointCallHandlerAdapter(OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this(null, (FallbackProvider) null, onCircuitBreakerMetadataResolver);
	}

	public HystrixCommandEndpointCallHandlerAdapter(Fallback fallback) {
		this(null, (FallbackProvider) (t) -> fallback, null);
	}

	public HystrixCommandEndpointCallHandlerAdapter(Fallback fallback,
			OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this(null, (FallbackProvider) (t) -> fallback, onCircuitBreakerMetadataResolver);
	}

	public HystrixCommandEndpointCallHandlerAdapter(FallbackProvider fallback) {
		this(null, fallback, null);
	}

	public HystrixCommandEndpointCallHandlerAdapter(FallbackProvider fallback,
			OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this(null, fallback, onCircuitBreakerMetadataResolver);
	}

	public HystrixCommandEndpointCallHandlerAdapter(HystrixCommand.Setter properties, Fallback fallback) {
		this(properties, (FallbackProvider) (t) -> fallback);
	}

	public HystrixCommandEndpointCallHandlerAdapter(HystrixCommand.Setter properties, FallbackProvider fallback) {
		this(properties, fallback, null);
	}

	private HystrixCommandEndpointCallHandlerAdapter(HystrixCommand.Setter properties, FallbackProvider fallback,
			OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver) {
		this.properties = properties;
		this.fallback = Optional.ofNullable(fallback).orElseGet(WithFallbackProvider::new);
		this.hystrixCommandMetadataFactory = Optional.ofNullable(onCircuitBreakerMetadataResolver)
				.map(HystrixCommandMetadataFactory::new)
					.orElseGet(HystrixCommandMetadataFactory::new);
	}

	@Override
	public final boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(HystrixCommand.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(unwrap(endpointMethod.returnType()));
	}

	private Type unwrap(JavaType declaredReturnType) {
		return declaredReturnType.parameterized() ?
				declaredReturnType.as(ParameterizedType.class).getActualTypeArguments()[0] :
					Object.class;
	}

	@Override
	public EndpointCallHandler<HystrixCommand<T>, O> adapt(EndpointMethod endpointMethod, EndpointCallHandler<T, O> delegate) {
		return new HystrixCommandEndpointCallHandler<T, O>(properties, endpointMethod, delegate,
				fallback, hystrixCommandMetadataFactory);
	}
}
