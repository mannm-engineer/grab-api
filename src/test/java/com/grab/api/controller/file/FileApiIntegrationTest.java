package com.grab.api.controller.file;

import static org.assertj.core.api.Assertions.assertThat;

import com.grab.api.integration.ApiTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApiTest
class FileApiIntegrationTest {

  private static final BasicJsonTester JSON_TESTER =
      new BasicJsonTester(FileApiIntegrationTest.class);

  @Autowired
  private RestTestClient restTestClient;

  @Autowired
  private JdbcClient jdbcClient;

  @Value("${app.storage.path:./uploads}")
  private String storagePath;

  @Test
  @Sql(statements = """
    INSERT INTO driver (full_name, mobile_phone, status, age, rating, is_verified, balance, date_of_birth, created_at, created_by)
    VALUES ('Jane Doe', '+6599887766', 'AVAILABLE', 25, 4.0, true, 500.00, '1995-05-20', now(), 'SYSTEM');
    INSERT INTO driver_document (driver_id, type, document_number, expiry_date)
    VALUES (1, 'NATIONAL_ID', 'N1234567B', '2030-12-31');
  """)
  void downloadFile_existingFile_responseOk() throws IOException {
    // ARRANGE — copy file to storage and insert file record
    var fileContent = new ClassPathResource("test-license.txt").getContentAsByteArray();
    var filePath = Path.of(storagePath).resolve("test-license.txt");
    Files.createDirectories(filePath.getParent());
    Files.write(filePath, fileContent);

    jdbcClient
        .sql("INSERT INTO file (document_id, file_url) VALUES (1, :fileUrl)")
        .param("fileUrl", filePath.toAbsolutePath().toString())
        .update();

    // ACT
    var responseSpec = restTestClient.get().uri("/files/1").exchange();

    // ASSERT
    var expectedContent = new ClassPathResource("test-license.txt").getContentAsByteArray();

    // @spotless:off
    responseSpec
      .expectStatus().isOk()
      .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
      .expectBody(byte[].class)
      .value(body -> assertThat(body).isEqualTo(expectedContent));
    // @spotless:on
  }

  @Test
  void downloadFile_nonExistingFile_responseNotFound() {
    // ACT
    var responseSpec = restTestClient
        .get()
        .uri("/files/999")
        .accept(MediaType.APPLICATION_JSON)
        .exchange();

    // ASSERT
    // @spotless:off
    responseSpec
      .expectStatus().isNotFound()
      .expectBody(String.class)
      .value(body ->
        assertThat(JSON_TESTER.from(body))
          .isStrictlyEqualToJson(
            // language=JSON
            """
            {
              "detail": "Resource with id 999 not found.",
              "instance": "/api/files/999",
              "status": 404,
              "title": "Not Found"
            }
            """));
    // @spotless:on
  }
}
