package com.restify.http.client;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

public class Headers {

	private final Collection<Header> headers = new LinkedHashSet<>();

	public void add(Header header) {
		headers.add(header);
	}

	public void put(String name, Collection<String> values) {
		values.forEach(value -> headers.add(new Header(name, value)));
	}

	public Collection<Header> all() {
		return Collections.unmodifiableCollection(headers);
	}

	public Optional<String> get(String name) {
		return headers.stream().filter(h -> h.name().equals(name))
				.findFirst()
					.map(Header::value);
	}

	@Override
	public String toString() {
		return headers.toString();
	}
}
