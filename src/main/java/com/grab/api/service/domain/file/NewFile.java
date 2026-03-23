package com.grab.api.service.domain.file;

import java.io.InputStream;

public record NewFile(String filename, InputStream content) {}
