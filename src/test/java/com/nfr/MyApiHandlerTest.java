package com.nfr;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the deployed Lambda entry point.
 *
 * Deliberately plain JUnit 5 — no @SpringBootTest. MyApiHandler has no Spring
 * involvement, so starting a context here would only slow the test down and
 * test the wrong thing (see NfrApplicationTests for that dead path).
 */
class MyApiHandlerTest {

    private final MyApiHandler handler = new MyApiHandler();
    private final LambdaLogger logger = mock(LambdaLogger.class);
    private final Context context = mock(Context.class);

    @BeforeEach
    void stubLogger() {
        // The handler logs unconditionally, so getLogger() must return something.
        when(context.getLogger()).thenReturn(logger);
    }

    @Test
    void returnsStatusCode200() {
        Map<String, Object> response = handler.handleRequest(new HashMap<>(), context);

        assertThat(response.get("statusCode")).isEqualTo(200);
    }

    @Test
    void returnsOnlyTheThreeApiGatewayProxyEnvelopeKeys() {
        Map<String, Object> response = handler.handleRequest(new HashMap<>(), context);

        assertThat(response).containsOnlyKeys("statusCode", "headers", "body");
    }

    @Test
    void returnsJsonContentTypeAndPermissiveCorsHeaders() {
        Map<String, Object> response = handler.handleRequest(new HashMap<>(), context);

        assertThat(response.get("headers"))
                .asInstanceOf(InstanceOfAssertFactories.map(String.class, String.class))
                .containsEntry("Content-Type", "application/json")
                .containsEntry("Access-Control-Allow-Origin", "*");
    }

    @Test
    void returnsTheHelloWorldMessageAsAJsonBodyString() {
        Map<String, Object> response = handler.handleRequest(new HashMap<>(), context);

        // The body is a hand-built JSON string literal rather than a serialised
        // object, so this pins the exact bytes the client receives. It detects any
        // edit to the payload; it does not by itself prove the string parses.
        assertThat(response.get("body"))
                .isEqualTo("{\"message\": \"Hello World from MyApiHandler!\"}");
    }

    @Test
    void logsTheRequestToCloudWatch() {
        handler.handleRequest(new HashMap<>(), context);

        verify(logger).log("Received a request on the Hello World API endpoint.");
    }

    @Test
    void ignoresTheRequestPayloadEntirely() {
        Map<String, Object> withQueryString = new HashMap<>();
        withQueryString.put("path", "/hello");
        withQueryString.put("queryStringParameters", Map.of("name", "Simon"));

        Map<String, Object> fromPayload = handler.handleRequest(withQueryString, context);
        Map<String, Object> fromEmpty = handler.handleRequest(new HashMap<>(), context);

        assertThat(fromPayload).isEqualTo(fromEmpty);
    }

    @Test
    void neverDereferencesTheInputMap() {
        // The response is built from constants alone. Passing null is not a real
        // API Gateway event — it is the cheapest proof that no code path reads
        // the input, so adding one later cannot silently NPE here.
        Map<String, Object> response = handler.handleRequest(null, context);

        assertThat(response.get("statusCode")).isEqualTo(200);
    }
}
