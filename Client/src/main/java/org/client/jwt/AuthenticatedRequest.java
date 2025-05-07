package org.client.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Log4j2
public class AuthenticatedRequest {
  @Setter
  private String authToken;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public AuthenticatedRequest() {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
  }

  public HttpResponse<String> makeAuthenticatedRequest(String url, String method, Object requestBody)
          throws IOException, InterruptedException {

    if (authToken == null || authToken.isEmpty()) {
      throw new IllegalStateException("Требуется аутентификация. Сначала выполните вход.");
    }

    String bodyJson = requestBody != null ? objectMapper.writeValueAsString(requestBody) : "";

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + authToken);

    switch (method.toUpperCase()) {
      case "GET":
        requestBuilder.GET();
        break;
      case "POST":
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(bodyJson));
        break;
      case "PUT":
        requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(bodyJson));
        break;
      case "DELETE":
        requestBuilder.DELETE();
        break;
      default:
        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }

    HttpRequest request = requestBuilder.build();

    log.debug("Sending {} request to {} with auth token", method, url);
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  public <T> T makeAuthenticatedRequest(String url, String method, Object requestBody, Class<T> responseType)
          throws IOException, InterruptedException {

    HttpResponse<String> response = makeAuthenticatedRequest(url, method, requestBody);
    return objectMapper.readValue(response.body(), responseType);
  }
}
