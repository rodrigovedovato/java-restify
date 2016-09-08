package com.restify.http.client.converter.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;

public class JacksonMessageConverter<T> extends JsonMessageConverter<T> {

	private final ObjectMapper objectMapper;

	public JacksonMessageConverter() {
		this(new ObjectMapper()
				.setSerializationInclusion(Include.NON_NULL)
				.configure(SerializationFeature.INDENT_OUTPUT, false));

		this.objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		this.objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	}

	public JacksonMessageConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean writerOf(Class<?> type) {
		return objectMapper.canSerialize(type);
	}

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		try {
			JsonEncoding encoding = Arrays.stream(JsonEncoding.values())
					.filter(e -> e.getJavaName().equals(httpRequestMessage.charset()))
					.findFirst()
					.orElse(JsonEncoding.UTF8);

			JsonGenerator generator = objectMapper.getFactory().createGenerator(httpRequestMessage.output(), encoding);

			objectMapper.writeValue(generator, body);
			generator.flush();

		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException(e);
		}

	}

	@Override
	public boolean readerOf(Type type) {
		TypeFactory typeFactory = objectMapper.getTypeFactory();
		return objectMapper.canDeserialize(typeFactory.constructType(type));
	}

	@Override
	public T read(Type expectedType, HttpResponseMessage httpResponseMessage) throws RestifyHttpMessageReadException {
		try {
			TypeFactory typeFactory = objectMapper.getTypeFactory();

			return objectMapper.readValue(httpResponseMessage.input(), typeFactory.constructType(expectedType));

		} catch (IOException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}

}
