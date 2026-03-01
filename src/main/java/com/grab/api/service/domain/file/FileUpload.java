package com.grab.api.service.domain.file;

import java.io.InputStream;

public record FileUpload(String filename, InputStream content) {}
