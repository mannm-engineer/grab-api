package com.grab.api.controller.driver;

import static org.hamcrest.Matchers.hasItem;
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
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ApiUnitTest(controllers = DriverRestController.class)
@MockitoBean(types = DriverService.class)
class DriverApiInputValidationTest {

  @Autowired
  private MockMvc mockMvc;

  private static MockPart driverDataPart(String json) {
    var part = new MockPart("data", json.getBytes());
    part.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    return part;
  }

  private static MockPart dummyFilePart() {
    return new MockPart("files", "dummy.txt", "dummy".getBytes());
  }

  static Stream<Arguments> createDriver_blankFields() {
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
  @MethodSource("createDriver_blankFields")
  void createDriver_blankFields_responseBadRequest(String scenario, String requestBody)
      throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/drivers")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .part(driverDataPart(requestBody))
            .part(dummyFilePart())
            .accept(MediaType.APPLICATION_JSON))
        // .andDo(print()) // enable this line to see the full MockMvc request/response details
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.fieldErrors", hasItem("fullName: must not be blank")))
        .andExpect(jsonPath("$.fieldErrors", hasItem("mobilePhone: must not be blank")));
  }

  static Stream<Arguments> createDriver_invalidMobilePhone() {
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
  @MethodSource("createDriver_invalidMobilePhone")
  void createDriver_invalidMobilePhone_responseBadRequest(String scenario, String requestBody)
      throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/drivers")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .part(driverDataPart(requestBody))
            .part(dummyFilePart())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath(
            "$.fieldErrors",
            hasItem("mobilePhone: must be a valid E.164 phone number (e.g., +6591234567)")));
  }

  static Stream<Arguments> createDriver_nullFields() {
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
  @MethodSource("createDriver_nullFields")
  void createDriver_nullFields_responseBadRequest(String scenario, String requestBody)
      throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/drivers")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .part(driverDataPart(requestBody))
            .part(dummyFilePart())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.fieldErrors", hasItem("age: must not be null")))
        .andExpect(jsonPath("$.fieldErrors", hasItem("rating: must not be null")))
        .andExpect(jsonPath("$.fieldErrors", hasItem("isVerified: must not be null")))
        .andExpect(jsonPath("$.fieldErrors", hasItem("balance: must not be null")))
        .andExpect(jsonPath("$.fieldErrors", hasItem("dateOfBirth: must not be null")));
  }

  static Stream<Arguments> createDriver_invalidAge() {
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
              "dateOfBirth": "1990-01-15",
              "documents": [
                {
                  "type": "DRIVERS_LICENSE",
                  "documentNumber": "S1234567A",
                  "expiryDate": "2030-01-01",
                  "filenames": ["license.pdf"]
                }
              ]
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
              "dateOfBirth": "1990-01-15",
              "documents": [
                {
                  "type": "DRIVERS_LICENSE",
                  "documentNumber": "S1234567A",
                  "expiryDate": "2030-01-01",
                  "filenames": ["license.pdf"]
                }
              ]
            }
            """,
            "age: must be less than or equal to 100"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("createDriver_invalidAge")
  void createDriver_invalidAge_responseBadRequest(
      String scenario, String requestBody, String expectedError) throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/drivers")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .part(driverDataPart(requestBody))
            .part(dummyFilePart())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.fieldErrors", hasItem(expectedError)));
  }

  static Stream<Arguments> createDriver_invalidRating() {
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
              "dateOfBirth": "1990-01-15",
              "documents": [
                {
                  "type": "DRIVERS_LICENSE",
                  "documentNumber": "S1234567A",
                  "expiryDate": "2030-01-01",
                  "filenames": ["license.pdf"]
                }
              ]
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
              "dateOfBirth": "1990-01-15",
              "documents": [
                {
                  "type": "DRIVERS_LICENSE",
                  "documentNumber": "S1234567A",
                  "expiryDate": "2030-01-01",
                  "filenames": ["license.pdf"]
                }
              ]
            }
            """,
            "rating: must be less than or equal to 5.0"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("createDriver_invalidRating")
  void createDriver_invalidRating_responseBadRequest(
      String scenario, String requestBody, String expectedError) throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/drivers")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .part(driverDataPart(requestBody))
            .part(dummyFilePart())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.fieldErrors", hasItem(expectedError)));
  }

  @Test
  void createDriver_missingDocumentFiles_responseBadRequest() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/drivers")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .part(driverDataPart(
                // language=JSON
                """
                {
                  "fullName": "John Doe",
                  "mobilePhone": "+6591234567",
                  "age": 30,
                  "rating": 4.5,
                  "isVerified": false,
                  "balance": 1000.50,
                  "dateOfBirth": "1990-01-15",
                  "documents": [
                    {
                      "type": "DRIVERS_LICENSE",
                      "documentNumber": "S1234567A",
                      "expiryDate": "2030-01-01",
                      "filenames": ["license.pdf"]
                    }
                  ]
                }
                """))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Required part 'files' is not present."));
  }
}
