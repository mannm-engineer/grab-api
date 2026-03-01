package com.grab.api.service.domain.file;

import java.io.InputStream;

public record FileCreate(String filename, InputStream content) {}
