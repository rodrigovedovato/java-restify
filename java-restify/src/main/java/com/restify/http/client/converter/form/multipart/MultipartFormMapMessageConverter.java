package com.restify.http.client.converter.form.multipart;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.restify.http.client.HttpRequestMessage;

public class MultipartFormMapMessageConverter extends MultipartFormMessageConverter<Map<String, ?>> {

	public MultipartFormMapMessageConverter() {
	}

	protected MultipartFormMapMessageConverter(MultipartFormBoundaryGenerator boundaryGenerator) {
		super(boundaryGenerator);
	}

	@Override
	public boolean writerOf(Class<?> type) {
		return Map.class == type && supportedMapKey(type);
	}

	private boolean supportedMapKey(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;

			Type mapKeyType = parameterizedType.getActualTypeArguments()[0];

			return (mapKeyType == String.class);

		} else {
			return true;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void doWrite(String boundary, Map<String, ?> body, HttpRequestMessage httpRequestMessage) throws IOException {
		body.forEach((key, value) -> {
			serializers.of(value.getClass())
				.write(boundary, new MultipartField(key, value), httpRequestMessage);
		});
	}
}
