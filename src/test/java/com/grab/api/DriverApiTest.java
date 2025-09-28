package com.grab.api;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

class DriverApiTest {

  static {
    System.setProperty("jdk.httpclient.HttpClient.log", "all");
  }

//  private static final HttpClient client = HttpClient.newHttpClient();
  private static final AtomicInteger count = new AtomicInteger(0);

  @Test
  void test() throws Exception {
    final var NUMBER_OF_THREAD = 10000;
    var threads = new Thread[NUMBER_OF_THREAD];

    for (int i = 0; i < NUMBER_OF_THREAD; i++) {
      threads[i] = new Thread(() -> incrementIfOk(send("/drivers")));
    }

    for (final var thread : threads) {
      thread.start();
      Thread.sleep(50); // stagger starts slightly
    }

    for (final var thread : threads) {
      thread.join();
    }

    System.out.println("Successful requests: " + count.get());
  }

  private static void incrementIfOk(final HttpResponse<String> response) {
    if (response != null && response.statusCode() == 200) {
      count.incrementAndGet();
    }
  }

  private static HttpResponse<String> send(String path) {
    try (var client = HttpClient.newHttpClient()) {
      var request = HttpRequest
        .newBuilder()
        .uri(URI.create("http://localhost:8080" + path))
        .GET()
        .build();

      return client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
