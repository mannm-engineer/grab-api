package com.grab.api.controller.ride;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.grab.api.controller.RideRestController;
import com.grab.api.service.RideService;
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

@ApiInputValidationTest(controllers = RideRestController.class)
@MockitoBean(types = RideService.class)
class CreateRideApiInputValidationTest {

  @Autowired
  private MockMvc mockMvc;

  private ResultActions postCreateRide(String requestBody) throws Exception {
    return mockMvc.perform(post("/rides")
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
                "pickupLocation": null,
                "dropoffLocation": null
              }
              """));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void responseBadRequest(String scenario, String requestBody) throws Exception {
      postCreateRide(requestBody)
          // .andDo(print()) // enable this line to see the full MockMvc request/response details
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value("Invalid request content."))
          .andExpect(jsonPath("$.fieldErrors", hasItem("pickupLocation: must not be null")))
          .andExpect(jsonPath("$.fieldErrors", hasItem("dropoffLocation: must not be null")));
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
                "passengerId": null
              }
              """),
          Arguments.of(
              "empty string",
              // language=JSON
              """
              {
                "passengerId": ""
              }
              """));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void responseBadRequest(String scenario, String requestBody) throws Exception {
      postCreateRide(requestBody)
          // .andDo(print()) // enable this line to see the full MockMvc request/response details
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value("Invalid request content."))
          .andExpect(jsonPath("$.fieldErrors", hasItem("passengerId: must not be blank")));
    }
  }
}
