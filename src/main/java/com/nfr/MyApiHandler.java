package com.nfr;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.HashMap;
import java.util.Map;

public class MyApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        // Log incoming request metadata to AWS CloudWatch for debugging
        context.getLogger().log("Received a request on the Hello World API endpoint.");

        // 1. Create the HTTP response envelope required by AWS API Gateway
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);

        // 2. Set headers to specify the content type and handle potential browser issues
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*"); // Allows calls from any frontend
        response.put("headers", headers);

        // 3. Define your actual payload/body response
        // Using a basic JSON string format
        response.put("body", "{\"message\": \"Hello World from your single-module Gradle Lambda!\"}");

        return response;
    }
}
