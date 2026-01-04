package com.grab.api.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@ApiTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(statements = """
  CREATE TABLE IF NOT EXISTS dummy
  (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
  );
""", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class DummyApiIntegrationTest {

  @TestConfiguration
  @EnableJdbcRepositories(considerNestedRepositories = true)
  @Import(DummyController.class)
  static class TestConfig {}

  @Autowired
  private RestTestClient restTestClient;

  @Autowired
  private JdbcClient jdbcClient;

  @RestController
  @RequestMapping("/__dummies")
  static class DummyController {

    private DummyRepository dummyRepository;

    public DummyController(DummyRepository dummyRepository) {
      this.dummyRepository = dummyRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createDummy(@RequestBody DummyCreateDTO dummyCreateDTO) {
      dummyRepository.save(dummyCreateDTO.entity());
    }
  }

  record DummyCreateDTO(String name) {

    public DummyEntity entity() {
      return new DummyEntity(null, name);
    }
  }

  @Table("dummy")
  record DummyEntity(@Id Integer id, String name) {}

  interface DummyRepository extends CrudRepository<DummyEntity, Integer> {}

  @Test
  void insertDummyViaController() {
    var request = new DummyCreateDTO("Dummy name");

    restTestClient
        .post()
        .uri("/__dummies")
        .body(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .isEmpty();

    var count =
        jdbcClient.sql("SELECT COUNT(*) FROM dummy").query(Integer.class).single();

    assertThat(count).isEqualTo(1);
  }

  @Test
  @Order(0)
  @Sql(statements = """
    INSERT INTO dummy(id, name) VALUES (1, 'Dummy name');
  """)
  void insertDummyData() {
    var count =
        jdbcClient.sql("SELECT COUNT(*) FROM dummy").query(Integer.class).single();

    assertThat(count).isEqualTo(1);
  }

  @Test
  @Order(1)
  void databaseIsCleanedAfterEachTest() {
    var count =
        jdbcClient.sql("SELECT COUNT(*) FROM dummy").query(Integer.class).single();

    assertThat(count).isEqualTo(0);
  }
}
