package com.grab.api.repository;

import com.grab.api.repository.entity.FileEntity;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface FileRepository extends Repository<FileEntity, Long> {

  Optional<FileEntity> findById(Long id);
}
