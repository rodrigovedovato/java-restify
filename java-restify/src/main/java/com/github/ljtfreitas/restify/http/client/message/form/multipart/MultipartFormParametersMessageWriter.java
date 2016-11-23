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
package com.github.ljtfreitas.restify.http.client.message.form.multipart;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.contract.MultipartParameters;

public class MultipartFormParametersMessageWriter extends MultipartFormMessageWriter<MultipartParameters> {

	private final MultipartFormMapMessageWriter mapMessageConverter = new MultipartFormMapMessageWriter();

	public MultipartFormParametersMessageWriter() {
	}

	protected MultipartFormParametersMessageWriter(MultipartFormBoundaryGenerator boundaryGenerator) {
		super(boundaryGenerator);
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return type == MultipartParameters.class;
	}

	@Override
	protected void doWrite(String boundary, MultipartParameters body, HttpRequestMessage httpRequestMessage)
			throws IOException {

		Map<String, Object> bodyAsMap = new LinkedHashMap<>();

		body.all().forEach(part -> bodyAsMap.put(part.name(), part.values()));

		mapMessageConverter.doWrite(boundary, bodyAsMap, httpRequestMessage);
	}
}
