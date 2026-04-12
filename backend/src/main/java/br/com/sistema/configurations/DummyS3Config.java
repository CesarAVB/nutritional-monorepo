package br.com.sistema.configurations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Slf4j
@Profile("!prod")
public class DummyS3Config {

    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    public S3Client dummyS3Client() {
        log.warn("MinIO not configured — registering dummy S3Client bean (non-prod). S3 operations will fail at runtime.");

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // Handle basic Object methods to avoid issues during bean lifecycle (hashCode, equals, toString)
                if (method.getDeclaringClass() == Object.class) {
                    String name = method.getName();
                    if ("toString".equals(name)) {
                        return "DummyS3ClientProxy";
                    }
                    if ("hashCode".equals(name)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(name)) {
                        return proxy == args[0];
                    }
                }
                throw new IllegalStateException("MinIO is not configured. Cannot perform S3 operation: " + method.getName());
            }
        };

        return (S3Client) Proxy.newProxyInstance(S3Client.class.getClassLoader(), new Class[] { S3Client.class }, handler);
    }

    @Bean
    @ConditionalOnMissingBean(S3Presigner.class)
    public S3Presigner dummyS3Presigner() {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    String name = method.getName();
                    if ("toString".equals(name)) {
                        return "DummyS3PresignerProxy";
                    }
                    if ("hashCode".equals(name)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(name)) {
                        return proxy == args[0];
                    }
                }
                throw new IllegalStateException("MinIO is not configured. Cannot perform S3 presign operation: " + method.getName());
            }
        };

        return (S3Presigner) Proxy.newProxyInstance(S3Presigner.class.getClassLoader(), new Class[] { S3Presigner.class }, handler);
    }
}