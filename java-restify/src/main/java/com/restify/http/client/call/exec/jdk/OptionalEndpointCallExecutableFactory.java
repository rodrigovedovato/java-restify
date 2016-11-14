package com.restify.http.client.call.exec.jdk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class OptionalEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<Optional<T>, T> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Optional.class);
	}

	@Override
	public EndpointCallExecutable<Optional<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new OptionalEndpointCallExecutable(JavaType.of(responseType));
	}

	private class OptionalEndpointCallExecutable implements EndpointCallExecutable<Optional<T>, T> {

		private final JavaType returnType;

		public OptionalEndpointCallExecutable(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public Optional<T> execute(EndpointCall<T> call, Object[] args) {
			return Optional.ofNullable(call.execute());
		}
	}
}