package com.grab.api.repository;

import com.grab.api.repository.entity.OutboxEventEntity;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends Repository<OutboxEventEntity, Long> {

  void save(OutboxEventEntity entity);

  @Query("SELECT * FROM outbox_event ORDER BY id LIMIT :limit FOR UPDATE SKIP LOCKED")
  List<OutboxEventEntity> findEventsForProcessing(@Param("limit") int limit);

  void delete(OutboxEventEntity entity);
}
