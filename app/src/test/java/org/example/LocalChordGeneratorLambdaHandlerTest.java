package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalChordGeneratorLambdaHandlerTest {
    private LocalChordGeneratorLambdaHandler handler;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new LocalChordGeneratorLambdaHandler();
        when(context.getLogger()).thenReturn(logger);
        doNothing().when(logger).log(anyString());
    }

    @Test
    void testHandleRequest_ValidInput() throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", "C");
        requestBody.put("scaleType", "MAJOR");
        requestBody.put("maxVariations", 2);
        requestBody.put("tuning", Arrays.asList("G3", "D4", "A4", "E5"));
        requestBody.put("stringCount", 4);
        requestBody.put("maxFretSpan", 4);
        requestBody.put("allowInversions", true);
        requestBody.put("allowPartialChords", false);
        requestBody.put("allowOpenStrings", true);
        requestBody.put("allowFullChords", true);


        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(objectMapper.writeValueAsString(requestBody));


        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);


        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
        assertEquals("C", responseBody.get("key"));
        assertEquals("MAJOR", responseBody.get("scale_type"));
    }

    // tests 3 strings defined in tuning but 4 in count of strings 
    @Test
    void testHandleRequest_StringCountMismatch() throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", "C");
        requestBody.put("scaleType", "MAJOR");
        requestBody.put("maxVariations", 2);
        requestBody.put("tuning", Arrays.asList("G3", "D4", "A4")); 
        requestBody.put("stringCount", 4); // 
        requestBody.put("maxFretSpan", 4);
        requestBody.put("allowInversions", true);
        requestBody.put("allowPartialChords", false);
        requestBody.put("allowOpenStrings", true);
        requestBody.put("allowFullChords", true);


        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(objectMapper.writeValueAsString(requestBody));


        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);


        assertEquals(400, response.getStatusCode());
        Map<String, Object> errorResponse = objectMapper.readValue(response.getBody(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        assertEquals("String count mismatch", errorResponse.get("error"));
    }

    // @Test
    // void testHandleRequest_Options() {
    //     APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
    //     request.setHttpMethod("OPTIONS");

    //     APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

    //     assertEquals(200, response.getStatusCode());
    //     assertEquals("{}", response.getBody());
    //     assertNotNull(response.getHeaders());
    //     assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Origin"));
    // }

    // @Test
    // void testHandleRequest_InvalidJson() {
    //     APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
    //     request.setBody("invalid json");

    //     APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

    //     assertEquals(500, response.getStatusCode());
    //     assertTrue(response.getBody().contains("Internal server error"));
    //     verify(logger).log(contains("Error processing request"));
    // }
} 