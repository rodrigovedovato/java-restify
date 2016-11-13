package com.restify.spring.autoconfigure;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.core.type.AnnotationMetadata;

import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.request.HttpClientRequestFactory;
import com.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.restify.http.contract.metadata.RestifyContractReader;
import com.restify.http.spring.client.call.exec.AsyncResultEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.DeferredResultEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.HttpHeadersEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.ListenableFutureEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.ListenableFutureTaskEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.ResponseEntityEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.WebAsyncTaskEndpointCallExecutableFactory;
import com.restify.http.spring.client.request.RestOperationsEndpointRequestExecutor;
import com.restify.http.spring.contract.SpringWebContractReader;
import com.restify.http.spring.contract.metadata.SpelDynamicParameterExpressionResolver;
import com.restify.http.spring.contract.metadata.SpringDynamicParameterExpressionResolver;
import com.restify.spring.autoconfigure.RestifyAutoConfiguration.RestifyAutoConfigurationRegistrar;
import com.restify.spring.autoconfigure.RestifyProperties.RestifyApiClient;
import com.restify.spring.configure.RestifyProxyBeanBuilder;
import com.restify.spring.configure.RestifyProxyFactoryBean;
import com.restify.spring.configure.RestifyableType;
import com.restify.spring.configure.RestifyableTypeScanner;

@Configuration
@Import(RestifyAutoConfigurationRegistrar.class)
@ConditionalOnMissingBean(RestifyProxyFactoryBean.class)
@AutoConfigureAfter(WebClientAutoConfiguration.class)
public class RestifyAutoConfiguration {

	@Configuration
	protected static class RestifySpringConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public EndpointRequestExecutor endpointRequestExecutor(RestTemplateBuilder restTemplateBuilder) {
			return new RestOperationsEndpointRequestExecutor(restTemplateBuilder.build());
		}

		@ConditionalOnMissingBean
		@Bean
		public RestifyContractReader restifyContractReader(SpringDynamicParameterExpressionResolver expressionResolver) {
			return new SpringWebContractReader(expressionResolver);
		}

		@ConditionalOnMissingBean
		@Bean
		public SpringDynamicParameterExpressionResolver expressionResolver(ConfigurableBeanFactory beanFactory) {
			return new SpelDynamicParameterExpressionResolver(beanFactory);
		}

		@ConditionalOnMissingBean
		@Bean
		public HttpClientRequestFactory httpClientRequestFactory() {
			return new JdkHttpClientRequestFactory();
		}

		@ConditionalOnMissingBean
		@Bean
		public HttpHeadersEndpointCallExecutableFactory httpHeadersEndpointCallExecutableFactory() {
			return new HttpHeadersEndpointCallExecutableFactory();
		}

		@ConditionalOnMissingBean
		@Bean
		public ResponseEntityEndpointCallExecutableFactory<Object> responseEntityEndpointCallExecutableFactory() {
			return new ResponseEntityEndpointCallExecutableFactory<>();
		}
	}

	@Configuration
	protected static class RestifySpringAsyncRequestConfiguration {

		@Value("${restify.async.timeout:}")
		private Long asyncTimeout;

		@ConditionalOnMissingBean(name = "restifyAsyncTaskExecutor", value = AsyncListenableTaskExecutor.class)
		@Bean
		public AsyncListenableTaskExecutor restifyAsyncTaskExecutor() {
			return new SimpleAsyncTaskExecutor("RestifyAsyncTaskExecutor");
		}

		@ConditionalOnMissingBean(name = "restifyAsyncExecutorService", value = ExecutorService.class)
		@Bean
		public ExecutorService restifyAsyncExecutorService(@Qualifier("restifyAsyncTaskExecutor") TaskExecutor executor) {
			return new ExecutorServiceAdapter(executor);
		}

		@ConditionalOnMissingBean
		@Bean
		public AsyncResultEndpointCallExecutableFactory<Object> asyncResultEndpointCallExecutableFactory() {
			return new AsyncResultEndpointCallExecutableFactory<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public DeferredResultEndpointCallExecutableFactory<Object> deferredResultEndpointCallExecutableFactory(Environment environment, 
				@Qualifier("restifyAsyncTaskExecutor") Executor executor) {
			return new DeferredResultEndpointCallExecutableFactory<>(asyncTimeout, executor);
		}

		@ConditionalOnMissingBean
		@Bean
		public ListenableFutureEndpointCallExecutableFactory<Object> listenableFutureEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
			return new ListenableFutureEndpointCallExecutableFactory<>(executor);
		}

		@ConditionalOnMissingBean
		@Bean
		public ListenableFutureTaskEndpointCallExecutableFactory<Object> listenableFutureTaskEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
			return new ListenableFutureTaskEndpointCallExecutableFactory<>(executor);
		}

		@ConditionalOnMissingBean
		@Bean
		public WebAsyncTaskEndpointCallExecutableFactory<Object> webAsyncTaskEndpointCallExecutableFactory(@Qualifier("restifyAsyncTaskExecutor") AsyncTaskExecutor executor) {
			return new WebAsyncTaskEndpointCallExecutableFactory<>(asyncTimeout, executor);
		}
	}

	protected static class RestifyAutoConfigurationRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {

		private static final Logger log = LoggerFactory.getLogger(RestifyAutoConfigurationRegistrar.class);

		private BeanFactory beanFactory;

		private RestifyProperties restifyProperties;

		private RestifyableTypeScanner scanner = new RestifyableTypeScanner();

		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
			AutoConfigurationPackages.get(beanFactory).forEach(p -> scan(p, registry));
		}

		private void scan(String packageName, BeanDefinitionRegistry registry) {
			scanner.findCandidateComponents(packageName)
				.stream()
					.map(candidate -> new RestifyableType(candidate.getBeanClassName()))
						.forEach(type -> create(type, registry));
		}

		private void create(RestifyableType type, BeanDefinitionRegistry registry) {
			RestifyApiClient restifyApiClient = restifyProperties.client(type);

			String endpoint = type.endpoint().map(e -> resolve(e)).orElseGet(restifyApiClient::getEndpoint);

			RestifyProxyBeanBuilder builder = new RestifyProxyBeanBuilder()
					.objectType(type.objectType())
						.endpoint(endpoint)
							.asyncExecutorServiceName("restifyAsyncExecutorService");

			registry.registerBeanDefinition(type.name(), builder.build());

			log.info("Create @Restifyable bean -> {} (API [{}] metadata: Description: [{}], and endpoint: [{}])",
					type.objectType(), type.name(), type.description(), endpoint);
		}

		private String resolve(String expression) {
			return restifyProperties.resolve(expression);
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		@Override
		public void setEnvironment(Environment environment) {
			this.restifyProperties = new RestifyProperties(environment);
		}
	}
}
