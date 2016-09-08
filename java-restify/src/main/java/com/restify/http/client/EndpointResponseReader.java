package com.restify.http.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.converter.text.TextPlainMessageConverter;

public class EndpointResponseReader {

	private final HttpMessageConverters converters;
	private final TextPlainMessageConverter errorResponseMessageConverter = new TextPlainMessageConverter();

	public EndpointResponseReader(HttpMessageConverters converters) {
		this.converters = converters;
	}

	public Object read(EndpointResponse response, Type expectedType) {
		return isVoid(expectedType) ? null : doRead(response, expectedType);
	}

	private boolean isVoid(Type expectedType) {
		return expectedType == Void.TYPE || expectedType == Void.class;
	}

	private Object doRead(EndpointResponse response, Type expectedType) {
		return ifSuccess(response.code(), r -> r.read(response, expectedType)).orElseThrow(() -> onError(response));
	}

	private Optional<Object> ifSuccess(EndpointResponseCode code, Function<EndpointSuccessResponseReader, Object> consumer) {
		return code.isSucess() ? Optional.of(consumer.apply(new EndpointSuccessResponseReader())) : Optional.empty();
	}

	private RestifyHttpException onError(EndpointResponse response) {
		String message = (String) errorResponseMessageConverter.read(String.class, response);
		return new RestifyHttpException("HTTP Status Code: " + response.code() + "\n" + message);
	}

	private class EndpointSuccessResponseReader {

		private Object read(EndpointResponse response, Type expectedType) {
			String contentType = response.headers().get("Content-Type").map(Header::value).orElse("text/plain");

			HttpMessageConverter<Object> converter = converters.readerOf(contentType, expectedType)
					.orElseThrow(() -> new RestifyHttpMessageReadException("Your request responded a content "
							+ "of type [" + contentType + "], but there is no MessageConverter able to read this message."));

			try {
				Object responseObject = converter.read(expectedType, response);

				response.input().close();

				return responseObject;

			} catch (IOException e) {
				throw new RestifyHttpMessageReadException("Error on try read http response body of type [" + contentType + "]", e);
			}
		}
	}


}
