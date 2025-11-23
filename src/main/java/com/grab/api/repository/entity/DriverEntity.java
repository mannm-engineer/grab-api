package com.grab.api.repository.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("driver")
public record DriverEntity(
  @Id Long id
) {}
