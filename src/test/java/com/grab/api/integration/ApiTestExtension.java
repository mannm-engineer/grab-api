package com.grab.api.integration;

import static org.springframework.jdbc.datasource.init.ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER;
import static org.springframework.jdbc.datasource.init.ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER;
import static org.springframework.jdbc.datasource.init.ScriptUtils.DEFAULT_COMMENT_PREFIX;
import static org.springframework.jdbc.datasource.init.ScriptUtils.EOF_STATEMENT_SEPARATOR;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class ApiTestExtension implements BeforeAllCallback, AfterAllCallback, AfterEachCallback {

  private SingleConnectionDataSource discreteDataSource;

  @Override
  public void beforeAll(ExtensionContext context) {
    var applicationContext = SpringExtension.getApplicationContext(context);
    var connectionDetails = applicationContext.getBean(JdbcConnectionDetails.class);

    discreteDataSource = new SingleConnectionDataSource(
        connectionDetails.getJdbcUrl(),
        connectionDetails.getUsername(),
        connectionDetails.getPassword(),
        true);
    discreteDataSource.setAutoCommit(false);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    discreteDataSource.destroy();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    try (var connection = DataSourceUtils.getConnection(discreteDataSource)) {
      ScriptUtils.executeSqlScript(
          connection,
          new EncodedResource(new ByteArrayResource(
              // language=PostgreSQL
              """
              DO
              $$
                DECLARE
                  truncate_command TEXT;
                BEGIN
                  SELECT 'TRUNCATE TABLE ' || string_agg(format('%I.%I', schemaname, tablename), ', ') || ' RESTART IDENTITY CASCADE;'
                  INTO truncate_command
                  FROM pg_tables
                  WHERE schemaname = 'public';

                  IF truncate_command IS NOT NULL THEN -- Only execute the truncate command if there are tables to delete
                    EXECUTE truncate_command;
                  END IF;
                END
              $$;
              """.getBytes())),
          false,
          false,
          DEFAULT_COMMENT_PREFIX,
          EOF_STATEMENT_SEPARATOR,
          DEFAULT_BLOCK_COMMENT_START_DELIMITER,
          DEFAULT_BLOCK_COMMENT_END_DELIMITER);

      connection.commit();
    }
  }
}
