package com.grab.api.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.grab.api.unit.ApiUnitTest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ApiUnitTest(controllers = DummyApiInputValidationTest.DummyController.class)
@Import(DummyApiInputValidationTest.DummyController.class)
class DummyApiInputValidationTest {

  @Autowired
  private MockMvc mockMvc;

  @RestController
  @RequestMapping("/__dummies")
  static class DummyController {

    @PostMapping
    void create(@RequestBody @Valid DummyCreateDTO dummyCreateDTO) {}
  }

  record DummyCreateDTO(@NotBlank String name) {}

  static Stream<Arguments> notBlankFieldScenarios() {
    return Stream.of(
        Arguments.of("missing required fields", "{}"),
        Arguments.of("null required fields", """
        {
          "name": null
        }
        """),
        Arguments.of("blank required fields", """
        {
          "name": ""
        }
        """));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("notBlankFieldScenarios")
  void createDummy_missingRequiredField_responseBadRequest(String scenario, String requestBody)
      throws Exception {

    mockMvc
        .perform(post("/__dummies")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody))
        // .andDo(print()) // enable this line to see the full MockMvc request/response details
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.fieldErrors", hasItem("name: must not be blank")));
  }
}
