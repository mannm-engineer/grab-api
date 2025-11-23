package com.grab.api;

import com.grab.api.controller.DriverRestController;
import com.grab.api.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DriverRestController.class)
@MockitoBean(types = DriverService.class)
class DriverApiInputValidationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void updateDriverLocation_latIsNull_responseBadRequest() throws Exception {
    var request = """
      {
        "lat": null,
        "lng": 10.0
      }
      """;

    mockMvc.perform(
      put("/drivers/1/location")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request)
    )
      .andExpect(status().isBadRequest());
  }

  @Test
  void updateDriverLocation_lngIsNull_responseBadRequest() throws Exception {
    var request = """
      {
        "lat": 10.0,
        "lng": null
      }
      """;

    mockMvc.perform(
      put("/drivers/1/location")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request)
    )
      .andExpect(status().isBadRequest());
  }
}
