package com.grab.api.service.domain.driver;

import com.grab.api.share.enumeration.DocumentType;
import java.time.LocalDate;

public record DriverDocument(
    DocumentType type, String documentNumber, LocalDate expiryDate, String fileUrl) {}
