package com.restify.http.client;

import java.io.OutputStream;

public interface HttpRequestMessage {

	public OutputStream output();

	public String charset();
}