package com.cfs.applicationservice.config;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import com.cfs.applicationservice.client.CustomerServiceClient;
import com.cfs.applicationservice.dto.CustomerOnboardingResponse;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import feign.Client;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.Decoder;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import javax.crypto.spec.SecretKeySpec;

import static org.assertj.core.api.Assertions.assertThat;

class FeignConfigTests {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void interceptorUsesOnlyCurrentAuthenticatedJwtAndDoesNotRetainTokens() throws Exception {
        RequestInterceptor interceptor = new FeignConfig().authorizationHeaderInterceptor();
        JwtAuthenticationToken first = customer("first-customer");
        JwtAuthenticationToken second = customer("second-customer");
        SecurityContextHolder.getContext().setAuthentication(first);
        RequestTemplate firstRequest = new RequestTemplate();
        interceptor.apply(firstRequest);
        assertThat(firstRequest.headers().get("Authorization"))
                .containsExactly("Bearer " + first.getToken().getTokenValue());
        SecurityContextHolder.getContext().setAuthentication(second);
        RequestTemplate secondRequest = new RequestTemplate();
        interceptor.apply(secondRequest);
        assertThat(secondRequest.headers().get("Authorization"))
                .containsExactly("Bearer " + second.getToken().getTokenValue());
        SecurityContextHolder.clearContext();
        assertNoHeader(interceptor);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("user", "unused", List.of()));
        assertNoHeader(interceptor);
        second.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(second);
        assertNoHeader(interceptor);
    }

    @Test
    void actualEnabledFeignCircuitBreakerCarriesOriginalCustomerContextAndClearsWorkers() throws Exception {
        Properties properties = new Properties();
        try (var input = getClass().getResourceAsStream("/application.properties")) {
            properties.load(input);
        }
        String enabled = properties.getProperty("spring.cloud.openfeign.circuitbreaker.enabled");
        assertThat(enabled).isEqualTo("true");
        JwtAuthenticationToken first = customer("first-customer");
        JwtAuthenticationToken second = customer("second-customer");
        Thread caller = Thread.currentThread();
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(FeignAutoConfiguration.class,
                        Resilience4JAutoConfiguration.class))
                .withUserConfiguration(RuntimeConfig.class)
                .withPropertyValues("spring.cloud.openfeign.circuitbreaker.enabled=" + enabled,
                        "spring.cloud.openfeign.client.config.customer-service.url=http://customer.test")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    CustomerServiceClient client = context.getBean(CustomerServiceClient.class);
                    Capture capture = context.getBean(Capture.class);
                    SecurityContextHolder.getContext().setAuthentication(first);
                    assertThat(client.getOnboardingStatus(1L)).isNotNull();
                    SecurityContextHolder.clearContext();
                    SecurityContextHolder.getContext().setAuthentication(second);
                    assertThat(client.getOnboardingStatus(2L)).isNotNull();
                    SecurityContextHolder.clearContext();
                    assertThat(client.getOnboardingStatus(3L)).isNotNull();
                    assertThat(capture.authentications.get(0)).isSameAs(first);
                    assertThat(capture.authentications.get(1)).isSameAs(second);
                    assertThat(capture.authentications.get(2)).isNull();
                    assertThat(capture.headers.get(0)).containsExactly("Bearer " + first.getToken().getTokenValue());
                    assertThat(capture.headers.get(1)).containsExactly("Bearer " + second.getToken().getTokenValue());
                    assertThat(capture.headers.get(2)).isEmpty();
                    assertThat(capture.threads).allMatch(thread -> thread != caller);
                    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
                    ExecutorService executor = context.getBean("securityContextCircuitBreakerExecutor",
                            ExecutorService.class);
                    assertThat(executor.submit(() -> SecurityContextHolder.getContext().getAuthentication()).get())
                            .isNull();
                    assertThat(context.getBean(CircuitBreakerRegistry.class).getAllCircuitBreakers())
                            .anyMatch(breaker -> breaker.getMetrics().getNumberOfSuccessfulCalls() == 3);
                });
    }

    private void assertNoHeader(RequestInterceptor interceptor) {
        RequestTemplate request = new RequestTemplate();
        interceptor.apply(request);
        assertThat(request.headers()).doesNotContainKey("Authorization");
    }

    private JwtAuthenticationToken customer(String subject) throws Exception {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        SignedJWT signed = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), new JWTClaimsSet.Builder()
                .subject(subject).expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("roles", List.of("ROLE_CUSTOMER")).build());
        signed.sign(new MACSigner(key));
        var jwt = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(key, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256).build().decode(signed.serialize());
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")), subject);
    }

    static class Capture {
        final List<Authentication> authentications = new CopyOnWriteArrayList<>();
        final List<List<String>> headers = new CopyOnWriteArrayList<>();
        final List<Thread> threads = new CopyOnWriteArrayList<>();
    }

    @Configuration
    @EnableFeignClients(clients = CustomerServiceClient.class)
    @Import({FeignConfig.class, CircuitBreakerSecurityConfig.class})
    static class RuntimeConfig {
        @Bean
        CircuitBreakerRegistry circuitBreakerRegistry() {
            return CircuitBreakerRegistry.ofDefaults();
        }

        @Bean
        TimeLimiterRegistry timeLimiterRegistry() {
            return TimeLimiterRegistry.ofDefaults();
        }

        @Bean
        Capture capture() {
            return new Capture();
        }

        @Bean
        Client client(Capture capture) {
            return (request, options) -> {
                capture.authentications.add(SecurityContextHolder.getContext().getAuthentication());
                capture.headers.add(List.copyOf(request.headers().getOrDefault("Authorization", List.of())));
                capture.threads.add(Thread.currentThread());
                return Response.builder().request(request).status(200).reason("OK").headers(Map.of())
                        .body(new byte[0]).build();
            };
        }

        @Bean
        Decoder decoder() {
            return (response, type) -> new CustomerOnboardingResponse();
        }
    }
}
