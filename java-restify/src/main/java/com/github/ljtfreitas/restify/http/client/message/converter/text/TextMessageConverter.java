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
package com.github.ljtfreitas.restify.http.client.message.converter.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.message.HttpMessageReader;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageWriter;
import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.RestifyHttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.RestifyHttpMessageReadException;

public abstract class TextMessageConverter implements HttpMessageReader<String>, HttpMessageWriter<String> {

	@Override
	public boolean canRead(Type type) {
		return String.class == type;
	}

	@Override
	public String read(HttpResponseMessage httpResponseMessage, Type expectedType) throws RestifyHttpMessageReadException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponseMessage.body()))) {
            return buffer.lines().collect(Collectors.joining("\n"));

		} catch (IOException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return String.class == type;
	}

	@Override
	public void write(String body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		try {
			httpRequestMessage.output().write(body.toString().getBytes());
		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException(e);
		}
	}
}
