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
package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_ALLOW_MAXIMUM_SIZE_TO_DIVERGE_FROM_CORE_SIZE;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_CORE_SIZE;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_KEEP_ALIVE_TIME_MINUTES;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_MAX_QUEUE_SIZE;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_METRICS_ROLLING_STATS_NUM_BUCKETS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_QUEUE_SIZE_REJECTION_THRESHOLD;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.netflix.hystrix.HystrixThreadPoolProperties;

class HystrixThreadPoolPropertyWriter {

	private static final Map<String, HystrixPropertyWriter<HystrixThreadPoolProperties.Setter>> HYSTRIX_THREAD_POOL_PROPERTIES_SETTERS
		= new LinkedHashMap<>();

	private final Map<String, String> properties;

	public HystrixThreadPoolPropertyWriter(Map<String, String> properties) {
		this.properties = properties;
	}

	public void applyTo(HystrixThreadPoolProperties.Setter hystrixtThreadPoolProperties) {
		properties.entrySet().stream()
			.forEach(property -> Optional.ofNullable(HYSTRIX_THREAD_POOL_PROPERTIES_SETTERS.get(property.getKey()))
					.ifPresent(s -> s.set(hystrixtThreadPoolProperties, property.getValue())));
	}

	static {
		HYSTRIX_THREAD_POOL_PROPERTIES_SETTERS.put(THREAD_POOL_MAX_QUEUE_SIZE,
				(s, v) -> s.withMaxQueueSize(Integer.valueOf(v)));

		HYSTRIX_THREAD_POOL_PROPERTIES_SETTERS.put(THREAD_POOL_CORE_SIZE,
				(s, v) -> s.withCoreSize(Integer.valueOf(v)));

		HYSTRIX_THREAD_POOL_PROPERTIES_SETTERS.put(THREAD_POOL_KEEP_ALIVE_TIME_MINUTES,
				(s, v) -> s.withKeepAliveTimeMinutes(Integer.valueOf(v)));

		HYSTRIX_THREAD_POOL_PROPERTIES_SETTERS.put(THREAD_POOL_QUEUE_SIZE_REJECTION_THRESHOLD,
				(s, v) -> s.withQueueSizeRejectionThreshold(Integer.valueOf(v)));

		HYSTRIX_THREAD_POOL_PROPERTIES_SETTERS.put(THREAD_POOL_ALLOW_MAXIMUM_SIZE_TO_DIVERGE_FROM_CORE_SIZE,
				(s, v) -> s.withAllowMaximumSizeToDivergeFromCoreSize(Boolean.valueOf(v)));

		HYSTRIX_THREAD_POOL_PROPERTIES_SETTERS.put(THREAD_POOL_METRICS_ROLLING_STATS_NUM_BUCKETS,
				(s, v) -> s.withMetricsRollingStatisticalWindowBuckets(Integer.valueOf(v)));

		HYSTRIX_THREAD_POOL_PROPERTIES_SETTERS.put(THREAD_POOL_METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS,
				(s, v) -> s.withMetricsRollingStatisticalWindowInMilliseconds(Integer.valueOf(v)));
	}
}
