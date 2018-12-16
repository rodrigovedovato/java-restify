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
package com.github.ljtfreitas.restify.http.contract;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MultipartParameters {

	private Map<String, List<Object>> parameters;

	public MultipartParameters() {
		this.parameters = new LinkedHashMap<>();
	}

	private MultipartParameters(Map<String, List<Object>> parameters) {
		this.parameters = new LinkedHashMap<>(parameters);
	}

	public MultipartParameters put(String name, String value) {
		return new MultipartParameters(this.parameters).doPut(name, value);
	}

	public MultipartParameters put(String name, File value) {
		return new MultipartParameters(this.parameters)
			.doPut(name, MultipartFile.create(name, value));
	}

	public MultipartParameters put(String name, File value, String contentType) {
		return new MultipartParameters(this.parameters)
			.doPut(name, MultipartFile.create(name, contentType, value));
	}

	public MultipartParameters put(String name, Path value) {
		return new MultipartParameters(this.parameters)
			.doPut(name, MultipartFile.create(name, value));
	}

	public MultipartParameters put(String name, Path value, String contentType) {
		return new MultipartParameters(this.parameters)
			.doPut(name, MultipartFile.create(name, contentType, value));
	}

	public MultipartParameters put(String name, String fileName, InputStream value) {
		return new MultipartParameters(this.parameters)
			.doPut(name, MultipartFile.create(name, fileName, value));
	}

	public MultipartParameters put(String name, String fileName, String contentType, InputStream value) {
		return new MultipartParameters(this.parameters)
			.doPut(name, MultipartFile.create(name, fileName, contentType, value));
	}

	public MultipartParameters put(MultipartFile multipartFile) {
		return new MultipartParameters(this.parameters)
			.doPut(multipartFile.name(), multipartFile);
	}

	private MultipartParameters doPut(String name, Object value) {
		parameters.compute(name, (k, v) -> Optional.ofNullable(v).orElseGet(ArrayList::new))
			.add(value);
		return this;
	}

	public Collection<Part<Object>> all() {
		return parameters.entrySet().stream()
			.map(e -> new Part<>(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	public class Part<T> {

		private final String name;
		private final Collection<T> values;

		private Part(String name, Collection<T> values) {
			this.name = name;
			this.values = values;
		}

		public String name() {
			return name;
		}

		public Collection<T> values() {
			return values;
		}
	}
}
