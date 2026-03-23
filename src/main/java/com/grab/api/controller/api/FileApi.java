package com.grab.api.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@Tag(name = "Files", description = "File management APIs")
public interface FileApi {

  @Operation(summary = "Download a file")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "File downloaded successfully",
        content = @Content(mediaType = "application/octet-stream")),
    @ApiResponse(
        responseCode = "404",
        description = "File not found",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  ResponseEntity<Resource> download(String id);
}
