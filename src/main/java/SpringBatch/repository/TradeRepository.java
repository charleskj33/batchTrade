package SpringBatch.repository;

import SpringBatch.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM TradeEntity t " +
            "WHERE t.clientName = :clientName " +
            "AND t.sourceSystem = :sourceSystem " +
            "AND t.insertedTimeStamp >= :startOfDay " +
            "AND t.insertedTimeStamp < :endOfDay")
    boolean existsForCLientToday(
            @Param("clientName") String clientName,
            @Param("sourceSystem") String sourceSystem,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
