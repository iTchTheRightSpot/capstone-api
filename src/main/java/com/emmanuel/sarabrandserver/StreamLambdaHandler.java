package com.emmanuel.sarabrandserver;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.serverless.proxy.spring.SpringBootProxyHandlerBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * For lambda documentation
 * <a href="https://github.com/awslabs/aws-serverless-java-container/wiki/Quick-start---Spring-Boot3">...</a>
 * */
public class StreamLambdaHandler implements RequestStreamHandler {
    private static final Logger log = LoggerFactory.getLogger(StreamLambdaHandler.class);
    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            handler = new SpringBootProxyHandlerBuilder<AwsProxyRequest>()
                    .defaultProxy()
                    .asyncInit()
                    .springBootApplication(Application.class)
                    .buildAndInitialize();
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            log.info("Exception thrown in StreamLambdaHandler, force another cold start {} ", e.getMessage());
            throw new RuntimeException("StreamLambdaHandler class ", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }

}
