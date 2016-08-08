package com.restify.http.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.metadata.EndpointMethodParameter.EndpointMethodParameterType;

public class EndpointHeaderParameterResolverTest {

	private EndpointMethodParameters parameters;

	@Before
	public void setup() {
		parameters = new EndpointMethodParameters();
	}

	@Test
	public void shouldResolveDynamicHeaderArgument() {
		parameters.put(new EndpointMethodParameter(0, "contentType", EndpointMethodParameterType.HEADER));

		EndpointHeaderParameterResolver resolver = new EndpointHeaderParameterResolver("{contentType}", parameters);

		Object[] args = new Object[] { "application/json" };

		String value = resolver.resolve(args);

		assertEquals(args[0], value);
	}

	@Test
	public void shouldResolveStaticHeaderArgumentOn() {
		parameters.put(new EndpointMethodParameter(0, "any", EndpointMethodParameterType.PATH));

		EndpointHeaderParameterResolver resolver = new EndpointHeaderParameterResolver("application/json", parameters);

		Object[] args = new Object[] { "no header argument" };

		String value = resolver.resolve(args);

		assertEquals("application/json", value);
	}
}