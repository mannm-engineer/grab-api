package com.grab.api.share.patch;

import org.jspecify.annotations.Nullable;

/**
 * Wrapper for patch operations that distinguishes between:
 * <ul>
 *   <li>Field absent → {@code null} reference (no instance created)</li>
 *   <li>Field explicitly set to {@code null} → {@code PatchField(null)}</li>
 *   <li>Field has a value → {@code PatchField(value)}</li>
 * </ul>
 */
public record PatchField<T>(@Nullable T value) {}
