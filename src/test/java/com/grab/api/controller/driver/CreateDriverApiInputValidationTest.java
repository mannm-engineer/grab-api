package com.grab.api.controller.driver;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.grab.api.controller.DriverRestController;
import com.grab.api.service.DriverService;
import com.grab.api.unit.ApiInputValidationTest;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ApiInputValidationTest(controllers = DriverRestController.class)
@MockitoBean(types = DriverService.class)
class CreateDriverApiInputValidationTest {

  @Autowired
  private MockMvc mockMvc;

  private ResultActions postCreateDriver(String requestBody) throws Exception {
    return mockMvc.perform(post("/drivers")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(requestBody));
  }

  @Nested
  class NullFields {

    static Stream<Arguments> scenarios() {
      return Stream.of(
          Arguments.of(
              "missing",
              // language=JSON
              """
              {}
              """),
          Arguments.of(
              "null",
              // language=JSON
              """
              {
                "age": null,
                "rating": null,
                "isVerified": null,
                "balance": null,
                "dateOfBirth": null
              }
              """));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void responseBadRequest(String scenario, String requestBody) throws Exception {
      postCreateDriver(requestBody)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value("Invalid request content."))
          .andExpect(jsonPath("$.fieldErrors", hasItem("age: must not be null")))
          .andExpect(jsonPath("$.fieldErrors", hasItem("rating: must not be null")))
          .andExpect(jsonPath("$.fieldErrors", hasItem("isVerified: must not be null")))
          .andExpect(jsonPath("$.fieldErrors", hasItem("balance: must not be null")))
          .andExpect(jsonPath("$.fieldErrors", hasItem("dateOfBirth: must not be null")));
    }
  }

  @Nested
  class BlankFields {

    static Stream<Arguments> scenarios() {
      return Stream.of(
          Arguments.of(
              "missing",
              // language=JSON
              """
              {}
              """),
          Arguments.of(
              "null",
              // language=JSON
              """
              {
                "fullName": null,
                "mobilePhone": null
              }
              """),
          Arguments.of(
              "empty string",
              // language=JSON
              """
              {
                "fullName": "",
                "mobilePhone": ""
              }
              """));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void responseBadRequest(String scenario, String requestBody) throws Exception {
      postCreateDriver(requestBody)
          // .andDo(print()) // enable this line to see the full MockMvc request/response details
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value("Invalid request content."))
          .andExpect(jsonPath("$.fieldErrors", hasItem("fullName: must not be blank")))
          .andExpect(jsonPath("$.fieldErrors", hasItem("mobilePhone: must not be blank")));
    }
  }

  @Nested
  class MobilePhone {

    static Stream<Arguments> invalidFormat() {
      return Stream.of(
          Arguments.of(
              "missing plus prefix",
              // language=JSON
              """
              { "mobilePhone": "6591234567" }
              """),
          Arguments.of(
              "contains spaces",
              // language=JSON
              """
              { "mobilePhone": "+65 9123 4567" }
              """),
          Arguments.of(
              "contains dashes",
              // language=JSON
              """
              { "mobilePhone": "+65-9123-4567" }
              """),
          Arguments.of(
              "starts with zero",
              // language=JSON
              """
              { "mobilePhone": "+0591234567" }
              """));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidFormat")
    void invalidFormat_responseBadRequest(String scenario, String requestBody) throws Exception {
      postCreateDriver(requestBody)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value("Invalid request content."))
          .andExpect(jsonPath(
              "$.fieldErrors",
              hasItem("mobilePhone: must be a valid E.164 phone number (e.g., +6591234567)")));
    }
  }

  @Nested
  class Age {

    static Stream<Arguments> outOfRange() {
      return Stream.of(
          Arguments.of(
              "age below minimum",
              // language=JSON
              """
              {
                "fullName": "John Doe",
                "mobilePhone": "+6591234567",
                "age": 17,
                "rating": 4.5,
                "isVerified": false,
                "balance": 1000.50,
                "dateOfBirth": "1990-01-15"
              }
              """,
              "age: must be greater than or equal to 18"),
          Arguments.of(
              "age above maximum",
              // language=JSON
              """
              {
                "fullName": "John Doe",
                "mobilePhone": "+6591234567",
                "age": 101,
                "rating": 4.5,
                "isVerified": false,
                "balance": 1000.50,
                "dateOfBirth": "1990-01-15"
              }
              """,
              "age: must be less than or equal to 100"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("outOfRange")
    void outOfRange_responseBadRequest(String scenario, String requestBody, String expectedError)
        throws Exception {
      postCreateDriver(requestBody)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value("Invalid request content."))
          .andExpect(jsonPath("$.fieldErrors", hasItem(expectedError)));
    }
  }

  @Nested
  class Rating {

    static Stream<Arguments> outOfRange() {
      return Stream.of(
          Arguments.of(
              "rating below minimum",
              // language=JSON
              """
              {
                "fullName": "John Doe",
                "mobilePhone": "+6591234567",
                "age": 30,
                "rating": -0.1,
                "isVerified": false,
                "balance": 1000.50,
                "dateOfBirth": "1990-01-15"
              }
              """,
              "rating: must be greater than or equal to 0.0"),
          Arguments.of(
              "rating above maximum",
              // language=JSON
              """
              {
                "fullName": "John Doe",
                "mobilePhone": "+6591234567",
                "age": 30,
                "rating": 5.1,
                "isVerified": false,
                "balance": 1000.50,
                "dateOfBirth": "1990-01-15"
              }
              """,
              "rating: must be less than or equal to 5.0"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("outOfRange")
    void outOfRange_responseBadRequest(String scenario, String requestBody, String expectedError)
        throws Exception {
      postCreateDriver(requestBody)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value("Invalid request content."))
          .andExpect(jsonPath("$.fieldErrors", hasItem(expectedError)));
    }
  }

  @Nested
  class DateOfBirth {

    static Stream<Arguments> invalidFormat() {
      return Stream.of(
          Arguments.of(
              "non-ISO format dd/MM/yyyy",
              // language=JSON
              """
              {
                "fullName": "John Doe",
                "mobilePhone": "+6591234567",
                "age": 30,
                "rating": 4.5,
                "isVerified": false,
                "balance": 1000.50,
                "dateOfBirth": "15/01/1990"
              }
              """),
          Arguments.of(
              "not a date",
              // language=JSON
              """
              {
                "fullName": "John Doe",
                "mobilePhone": "+6591234567",
                "age": 30,
                "rating": 4.5,
                "isVerified": false,
                "balance": 1000.50,
                "dateOfBirth": "not-a-date"
              }
              """));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidFormat")
    void invalidFormat_responseBadRequest(String scenario, String requestBody) throws Exception {
      postCreateDriver(requestBody)
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value("Failed to read request"));
    }
  }
}
