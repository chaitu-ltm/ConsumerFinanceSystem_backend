package com.cfs.notification.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import com.cfs.notification.client.CustomerClient;
import com.cfs.notification.client.CustomerClientConfiguration;
import com.cfs.notification.client.CustomerIdentityResponse;
import com.cfs.notification.config.SecurityConfig;
import com.cfs.notification.controller.NotificationController;
import com.cfs.notification.entity.Notification;
import com.cfs.notification.entity.NotificationStatus;
import com.cfs.notification.entity.NotificationType;
import com.cfs.notification.exception.GlobalExceptionHandler;
import com.cfs.notification.mapper.NotificationMapper;
import com.cfs.notification.repository.NotificationRepository;
import com.cfs.notification.service.NotificationService;
import com.cfs.notification.service.impl.NotificationServiceImpl;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import tools.jackson.databind.json.JsonMapper;

@SpringJUnitConfig(NotificationAuthorizationTests.TestConfig.class)
@WebAppConfiguration
class NotificationAuthorizationTests {

    private static final SecretKey KEY = Jwts.SIG.HS256.key().build();
    private static final String BODY = """
            {"customerId":20,"type":"EMI_DUE_REMINDER","channel":"IN_APP",
             "subject":"Due","message":"Payment due"}
            """;

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, JwtAuthenticationFilter.class, NotificationController.class,
            NotificationMapper.class, NotificationOwnership.class, NotificationServiceImpl.class,
            GlobalExceptionHandler.class, PublicEndpoints.class})
    static class TestConfig {
        @Bean JwtService jwtService() { return spy(new JwtService(Encoders.BASE64.encode(KEY.getEncoded()))); }
        @Bean NotificationRepository repository() { return mock(NotificationRepository.class); }
        @Bean CustomerClient customerClient() { return mock(CustomerClient.class); }
    }

    @org.springframework.web.bind.annotation.RestController
    static class PublicEndpoints {
        @org.springframework.web.bind.annotation.GetMapping({
                "/actuator/health", "/swagger-ui/index.html", "/v3/api-docs"})
        String publicEndpoint() { return "OK"; }
    }

    @Autowired WebApplicationContext context;
    @Autowired Filter springSecurityFilterChain;
    @Autowired NotificationRepository repository;
    @Autowired CustomerClient customerClient;
    @Autowired NotificationService service;
    @Autowired JwtService jwtService;

    private MockMvc mvc;
    private Notification notification;

    @BeforeEach
    void setUp() {
        reset(repository, customerClient);
        clearInvocations(jwtService);
        SecurityContextHolder.clearContext();
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        notification = new Notification();
        notification.setNotificationId(1L);
        notification.setCustomerId(20L);
        notification.setStatus(NotificationStatus.SENT);
        when(repository.findById(1L)).thenReturn(Optional.of(notification));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerClient.getCustomerByUserId(7L)).thenReturn(identity(20L, 7L));
        when(repository.findByCustomerIdOrderByCreatedAtDesc(20L)).thenReturn(List.of(notification));
        when(repository.findByCustomerIdAndTypeOrderByCreatedAtDesc(20L, NotificationType.EMI_DUE_REMINDER))
                .thenReturn(List.of(notification));
        when(repository.countByCustomerIdAndStatus(20L, NotificationStatus.SENT)).thenReturn(1L);
    }

    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    private static CustomerIdentityResponse identity(Long customerId, Long userId) {
        var response = new CustomerIdentityResponse();
        response.setCustomerId(customerId);
        response.setUserId(userId);
        return response;
    }

    private static String token(Object userId, Object roles) {
        return Jwts.builder().subject("not-a-customer-id").claim("userId", userId).claim("roles", roles)
                .expiration(Date.from(Instant.now().plusSeconds(300))).signWith(KEY).compact();
    }

    private static String customerToken() { return token(7, List.of("ROLE_CUSTOMER")); }
    private static String adminToken() { return token(8, List.of("ROLE_ADMIN")); }

    private static MockHttpServletRequestBuilder request(String operation, Long id) {
        return switch (operation) {
            case "get" -> get("/api/notifications/" + id);
            case "read" -> patch("/api/notifications/" + id + "/read");
            case "delete" -> delete("/api/notifications/" + id);
            case "list" -> get("/api/notifications/customer/" + id);
            case "type" -> get("/api/notifications/customer/" + id + "/type").param("type", "EMI_DUE_REMINDER");
            case "count" -> get("/api/notifications/customer/" + id + "/unread-count");
            default -> throw new IllegalArgumentException(operation);
        };
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "read", "delete", "list", "type", "count"})
    void ownCustomerAllowed(String operation) throws Exception {
        mvc.perform(request(operation, operation.equals("get") || operation.equals("read")
                || operation.equals("delete") ? 1L : 20L).header("Authorization", "Bearer " + customerToken()))
                .andExpect(status().is(operation.equals("delete") ? 204 : 200));
        verify(customerClient).getCustomerByUserId(7L);
        if (operation.equals("read")) {
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);
            assertThat(notification.getReadAt()).isNotNull();
        }
        if (operation.equals("delete")) { verify(repository).delete(notification); }
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "read", "delete", "list", "type", "count"})
    void otherCustomerDeniedBeforeQueryOrMutation(String operation) throws Exception {
        notification.setCustomerId(21L);
        mvc.perform(request(operation, operation.equals("get") || operation.equals("read")
                || operation.equals("delete") ? 1L : 21L).header("Authorization", "Bearer " + customerToken()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("Access denied"));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any(Notification.class));
        verify(repository, never()).findByCustomerIdOrderByCreatedAtDesc(any());
        verify(repository, never()).findByCustomerIdAndTypeOrderByCreatedAtDesc(any(), any());
        verify(repository, never()).countByCustomerIdAndStatus(any(), any());
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getReadAt()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "read", "delete", "list", "type", "count"})
    void adminCrossCustomerAllowedWithoutLookup(String operation) throws Exception {
        mvc.perform(request(operation, operation.equals("get") || operation.equals("read")
                || operation.equals("delete") ? 1L : 20L).header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().is(operation.equals("delete") ? 204 : 200));
        verifyNoInteractions(customerClient);
    }

    @Test void adminCreatesAndLists() throws Exception {
        mvc.perform(post("/api/notifications/internal").header("Authorization", "Bearer " + adminToken())
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.sentAt").exists()).andExpect(jsonPath("$.channel").value("IN_APP"));
        mvc.perform(get("/api/notifications").header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());
        verifyNoInteractions(customerClient);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_CUSTOMER", "ROLE_STAFF", "ROLE_EMI"})
    void nonAdminCannotCreateOrListAll(String role) throws Exception {
        String jwt = token(7, List.of(role));
        mvc.perform(post("/api/notifications/internal").header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
        mvc.perform(get("/api/notifications").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());
        verifyNoInteractions(repository, customerClient);
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "read", "delete", "list", "type", "count"})
    void staffCannotAccessCustomerOperations(String operation) throws Exception {
        mvc.perform(request(operation, 1L).header("Authorization", "Bearer " + token(7, List.of("ROLE_STAFF"))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(repository, customerClient);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Basic dXNlcjpwYXNz", "Bearer garbage", "Bearer "})
    void missingBasicAndMalformedAuthenticationReturnJson401(String header) throws Exception {
        mvc.perform(get("/api/notifications/1").header("Authorization", header))
                .andExpect(status().isUnauthorized()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().doesNotExist("Location"));
        mvc.perform(post("/api/notifications/internal").header("Authorization", header)
                .contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isUnauthorized());
        verifyNoInteractions(repository, customerClient);
    }

    @Test void noAuthorizationReturns401() throws Exception {
        mvc.perform(get("/api/notifications/1")).andExpect(status().isUnauthorized());
    }

    @Test void invalidSignedClaimsReturn401() throws Exception {
        for (Object id : List.of(7.5, new BigDecimal("9223372036854775808"), "7", -1, true)) {
            mvc.perform(get("/api/notifications/1").header("Authorization", "Bearer " + token(id, List.of("ROLE_CUSTOMER"))))
                    .andExpect(status().isUnauthorized());
        }
        for (Object roles : List.of("ROLE_CUSTOMER", List.of(7), List.of("CUSTOMER"), List.of("ROLE_"))) {
            mvc.perform(get("/api/notifications/1").header("Authorization", "Bearer " + token(7, roles)))
                    .andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(repository, customerClient);
    }

    @Test void expiredWrongSignatureAndMissingSubjectReturn401() throws Exception {
        var invalidTokens = List.of(
                Jwts.builder().subject("user").claim("roles", List.of("ROLE_CUSTOMER")).claim("userId", 7)
                        .expiration(Date.from(Instant.now().minusSeconds(60))).signWith(KEY).compact(),
                Jwts.builder().subject("user").claim("roles", List.of("ROLE_CUSTOMER")).claim("userId", 7)
                        .signWith(Jwts.SIG.HS256.key().build()).compact(),
                Jwts.builder().claim("roles", List.of("ROLE_CUSTOMER")).claim("userId", 7).signWith(KEY).compact());
        for (String jwt : invalidTokens) {
            mvc.perform(get("/api/notifications/1").header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 404})
    void deniedOrAbsentProfileFailsClosed(int upstreamStatus) throws Exception {
        when(customerClient.getCustomerByUserId(7L)).thenThrow(upstream(upstreamStatus));
        mvc.perform(get("/api/notifications/customer/20").header("Authorization", "Bearer " + customerToken()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("Access denied"));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 503, -1})
    void unavailableLookupIsSanitized503(int upstreamStatus) throws Exception {
        when(customerClient.getCustomerByUserId(7L)).thenThrow(upstreamStatus == -1
                ? new IllegalStateException("secret upstream timeout") : upstream(upstreamStatus));
        mvc.perform(get("/api/notifications/customer/20").header("Authorization", "Bearer " + customerToken()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Customer identity lookup unavailable"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));
        verifyNoInteractions(repository);
    }

    private static FeignException upstream(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://customer/secret", java.util.Map.of(),
                null, StandardCharsets.UTF_8, null);
        return FeignException.errorStatus("lookup", Response.builder().request(request).status(status)
                .reason("secret").body("secret upstream body", StandardCharsets.UTF_8).build());
    }

    @Test void inconsistentIdentityDeniedAndIncompleteContractUnavailable() throws Exception {
        for (CustomerIdentityResponse response : List.of(identity(20L, 8L), identity(21L, 7L), identity(-1L, 7L))) {
            when(customerClient.getCustomerByUserId(7L)).thenReturn(response);
            mvc.perform(get("/api/notifications/customer/20").header("Authorization", "Bearer " + customerToken()))
                    .andExpect(status().isForbidden());
        }
        when(customerClient.getCustomerByUserId(7L)).thenReturn(identity(null, 7L));
        mvc.perform(get("/api/notifications/customer/20").header("Authorization", "Bearer " + customerToken()))
                .andExpect(status().isServiceUnavailable());
        when(customerClient.getCustomerByUserId(7L)).thenReturn(null);
        mvc.perform(get("/api/notifications/customer/20").header("Authorization", "Bearer " + customerToken()))
                .andExpect(status().isServiceUnavailable());
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "read", "delete"})
    void missingNotificationRemains404BeforeOwnership(String operation) throws Exception {
        mvc.perform(request(operation, 99L).header("Authorization", "Bearer " + customerToken()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Notification not found with ID: 99"));
        verifyNoInteractions(customerClient);
    }

    @Test void missingSignedUserIdCannotUseSubject() throws Exception {
        mvc.perform(get("/api/notifications/customer/20").header("Authorization",
                "Bearer " + token(null, List.of("ROLE_CUSTOMER")))).andExpect(status().isForbidden());
        verifyNoInteractions(customerClient, repository);
    }

    @Test void interceptorReadsTrustedTokenAtInvocationAndDoesNotRetainIt() throws Exception {
        var interceptor = new CustomerClientConfiguration().customerBearerToken();
        String first = customerToken();
        String second = token(7, List.of("ROLE_CUSTOMER", "ROLE_EXTRA"));
        when(customerClient.getCustomerByUserId(7L)).thenAnswer(invocation -> {
            var jwt = NotificationIdentity.current();
            assertThat(jwt.getUserId()).isEqualTo(7L);
            assertThat(jwt.getName()).isEqualTo("not-a-customer-id");
            var template = new RequestTemplate();
            interceptor.apply(template);
            assertThat(template.headers().get("Authorization")).containsExactly("Bearer " + jwt.getBearerToken());
            return identity(20L, 7L);
        });
        for (String jwt : List.of(first, second)) {
            mvc.perform(get("/api/notifications/customer/20").header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk());
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
        assertThatThrownBy(() -> interceptor.apply(new RequestTemplate())).isInstanceOf(AccessDeniedException.class);
        verify(customerClient, times(2)).getCustomerByUserId(7L);
    }

    @Test void serviceRejectsOtherRolesAndUntrustedSpringTokens() {
        var authority = new SimpleGrantedAuthority("ROLE_CUSTOMER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("7", null, List.of(authority)));
        assertThatThrownBy(() -> service.getNotificationsByCustomer(20L)).isInstanceOf(AccessDeniedException.class);
        SecurityContextHolder.getContext().setAuthentication(
                new NotificationJwtAuthenticationToken("user", 7L, customerToken(),
                        List.of(new SimpleGrantedAuthority("ROLE_STAFF"))));
        assertThatThrownBy(() -> service.getNotificationsByCustomer(20L)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(service::getAllNotifications).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.createNotification(null)).isInstanceOf(AccessDeniedException.class);
        var jwt = new NotificationJwtAuthenticationToken("user", 7L, customerToken(), List.of(authority));
        jwt.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(jwt);
        assertThatThrownBy(() -> service.getUnreadCount(20L)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(repository, customerClient);
    }

    @Test void generic500DoesNotExposeRepositoryDetails() throws Exception {
        when(repository.findById(1L)).thenThrow(new IllegalStateException("secret database details"));
        mvc.perform(get("/api/notifications/1").header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test void signingKeyAndExactContractValidation() {
        assertThatThrownBy(() -> new JwtService(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtService(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtService("invalid")).isInstanceOf(RuntimeException.class);
        var response = new CustomerIdentityResponse();
        assertThatThrownBy(() -> response.setUserId(7.5)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> response.setCustomerId("20")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> response.setUserId(new BigDecimal("9223372036854775808")))
                .isInstanceOf(ArithmeticException.class);
        String jwt = token(new BigDecimal("7.0"), List.of("ROLE_CUSTOMER", "ROLE_EXTRA"));
        assertThat(jwtService.extractUserId(jwtService.extractClaims(jwt))).isEqualTo(7L);
        assertThat(jwtService.extractRoles(jwt)).containsExactly("ROLE_CUSTOMER", "ROLE_EXTRA");
    }

    @Test void filterParsesSignedClaimsExactlyOnce() throws Exception {
        String jwt = customerToken();
        mvc.perform(get("/api/notifications/customer/20").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());
        verify(jwtService, times(1)).extractClaims(jwt);
        verify(jwtService, never()).extractUsername(any());
        verify(jwtService, never()).extractRoles(any(String.class));
    }

    @Test void realFeignInvocationForwardsCurrentBearerAndRejectsDecodeContractErrors() throws Exception {
        var sentTokens = new java.util.ArrayList<String>();
        var responseBody = new java.util.concurrent.atomic.AtomicReference<>(
                "{\"customerId\":20,\"userId\":7,\"ignoredProfileField\":\"ignored\"}");
        var config = new CustomerClientConfiguration();
        CustomerClient client = config.customerFeignBuilder()
                .contract(new SpringMvcContract())
                .requestInterceptor(config.customerBearerToken())
                .client((outgoing, options) -> {
                    assertThat(outgoing.url()).endsWith("/api/customers/user/7");
                    sentTokens.add(outgoing.headers().get("Authorization").iterator().next());
                    return Response.builder().request(outgoing).status(200).reason("OK")
                            .body(responseBody.get(), StandardCharsets.UTF_8).build();
                })
                .decoder((response, type) -> JsonMapper.builder().build().readValue(
                        response.body().asInputStream(), CustomerIdentityResponse.class))
                .target(CustomerClient.class, "http://customer");
        when(customerClient.getCustomerByUserId(7L)).thenAnswer(invocation -> client.getCustomerByUserId(7L));
        String first = customerToken();
        String second = token(7, List.of("ROLE_CUSTOMER", "ROLE_EXTRA"));
        for (String jwt : List.of(first, second)) {
            mvc.perform(get("/api/notifications/customer/20").header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk());
        }
        assertThat(sentTokens).containsExactly("Bearer " + first, "Bearer " + second);
        responseBody.set("{\"customerId\":20,\"userId\":7.5}");
        mvc.perform(get("/api/notifications/customer/20").header("Authorization", "Bearer " + first))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Customer identity lookup unavailable"));
        assertThatThrownBy(() -> client.getCustomerByUserId(7L)).isInstanceOf(AccessDeniedException.class);
        assertThat(sentTokens).hasSize(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/health", "/swagger-ui/index.html", "/v3/api-docs"})
    void healthAndSwaggerRemainPublic(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk());
    }
}
