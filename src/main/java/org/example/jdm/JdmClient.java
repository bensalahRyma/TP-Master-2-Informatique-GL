package org.example.jdm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Simple HTTP client for the JeuxDeMots API.
 * This client returns raw JSON as String or parsed JsonNode.
 */
public class JdmClient {

    private static final String BASE_URL = "https://jdm-api.demo.lirmm.fr";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public JdmClient() {
        this(HttpClient.newHttpClient(), new ObjectMapper());
    }

    public JdmClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public String getTermRaw(String term) {
        return getRaw("/term/" + encode(term));
    }

    public String getRelationsRaw(String term) {
        return getRaw("/term/" + encode(term) + "/relations");
    }

    public String getSynonymsRaw(String term) {
        return getRaw("/term/" + encode(term) + "/r_syn");
    }

    public String getAntonymsRaw(String term) {
        return getRaw("/term/" + encode(term) + "/r_anto");
    }

    public String getAssociationsRaw(String term) {
        return getRaw("/term/" + encode(term) + "/r_associated");
    }

    public JsonNode getTerm(String term) {
        return parseJson(getTermRaw(term));
    }

    public JsonNode getRelations(String term) {
        return parseJson(getRelationsRaw(term));
    }

    public JsonNode getSynonyms(String term) {
        return parseJson(getSynonymsRaw(term));
    }

    public JsonNode getAntonyms(String term) {
        return parseJson(getAntonymsRaw(term));
    }

    public JsonNode getAssociations(String term) {
        return parseJson(getAssociationsRaw(term));
    }

    protected String getRaw(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            throw new JdmApiException("Unexpected status code: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JdmApiException("Error calling JDM API", e);
        }
    }

    protected JsonNode parseJson(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (IOException e) {
            throw new JdmApiException("Error parsing JSON", e);
        }
    }

    private String encode(String value) {
        return value.replace(" ", "%20");
    }
}
