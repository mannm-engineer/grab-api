package com.grab.api.repository;

import com.grab.api.service.domain.driver.Driver;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface DriverHttpClient {

  @GetExchange("/drivers")
  List<Driver> findDrivers(
      @RequestParam @Nullable String status, @RequestParam @Nullable Boolean hasLocation);

  @GetExchange("/drivers/{id}")
  @Nullable
  Driver getDriver(@PathVariable String id);

  @PostExchange("/drivers")
  Driver createDriver(@RequestBody Driver driver);

  @PutExchange("/drivers/{id}")
  void updateDriver(@PathVariable String id, @RequestBody Driver driver);
}
