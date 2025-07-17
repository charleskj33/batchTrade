package SpringBatch.repository;

import SpringBatch.entity.TrackerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackerRepo extends JpaRepository<TrackerEntity, Long> {
    Optional<TrackerEntity> findByBatchId(String batchId);
}
