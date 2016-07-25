package com.restify.http.client.converter.json;

import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.metadata.reflection.JavaClassDiscovery;

public abstract class JsonMessageConverter implements HttpMessageConverter {

	@Override
	public String contentType() {
		return "application/json";
	}

	public static JsonMessageConverter available() {
		return JavaClassDiscovery.present("com.fasterxml.jackson.databind.ObjectMapper") ? new JacksonMessageConverter()
			: JavaClassDiscovery.present("com.google.gson.Gson") ? new GsonMessageConverter()
				:  new FallbackJsonMessageConverter();
	}
}
