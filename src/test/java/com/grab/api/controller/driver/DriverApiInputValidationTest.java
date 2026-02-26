package com.grab.api.controller.driver;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.grab.api.controller.DriverRestController;
import com.grab.api.service.DriverService;
import com.grab.api.unit.ApiUnitTest;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ApiUnitTest(controllers = DriverRestController.class)
@MockitoBean(types = DriverService.class)
class DriverApiInputValidationTest {

  private static final MediaType MERGE_PATCH_JSON =
      MediaType.valueOf("application/merge-patch+json");

  @Autowired
  private MockMvc mockMvc;

  static Stream<Arguments> createDriver_notBlankFieldScenarios() {
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
              "mapId": null,
              "fullName": null,
              "mobilePhone": null
            }
            """),
        Arguments.of(
            "empty string",
            // language=JSON
            """
            {
              "mapId": "",
              "fullName": "",
              "mobilePhone": ""
            }
            """));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("createDriver_notBlankFieldScenarios")
  void createDriver_missingRequiredField_responseBadRequest(String scenario, String requestBody)
      throws Exception {
    mockMvc
        .perform(post("/drivers")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody))
        // .andDo(print()) // enable this line to see the full MockMvc request/response details
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.fieldErrors", hasItem("mapId: must not be blank")))
        .andExpect(jsonPath("$.fieldErrors", hasItem("fullName: must not be blank")))
        .andExpect(jsonPath("$.fieldErrors", hasItem("mobilePhone: must not be blank")));
  }

  static Stream<Arguments> createDriver_invalidMobilePhoneScenarios() {
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
  @MethodSource("createDriver_invalidMobilePhoneScenarios")
  void createDriver_invalidMobilePhone_responseBadRequest(String scenario, String requestBody)
      throws Exception {
    mockMvc
        .perform(post("/drivers")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath(
            "$.fieldErrors",
            hasItem("mobilePhone: must be a valid E.164 phone number (e.g., +6591234567)")));
  }

  @Test
  void patchDriver_locationIsNull_responseBadRequest() throws Exception {
    mockMvc
        .perform(patch("/drivers/1")
            .contentType(MERGE_PATCH_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(
                // language=JSON
                """
                {
                  "location": null
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.fieldErrors", hasItem("location: must not be null")));
  }

  @Test
  void patchDriver_locationLatAndLngAreNull_responseBadRequest() throws Exception {
    mockMvc
        .perform(patch("/drivers/1")
            .contentType(MERGE_PATCH_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(
                // language=JSON
                """
                {
                  "location": {
                    "lat": null,
                    "lng": null
                  }
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.fieldErrors", hasItem("location.lat: must not be null")))
        .andExpect(jsonPath("$.fieldErrors", hasItem("location.lng: must not be null")));
  }
}
