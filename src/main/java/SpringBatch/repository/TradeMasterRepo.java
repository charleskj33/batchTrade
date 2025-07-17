package SpringBatch.repository;
import SpringBatch.entity.TradeFeedMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface TradeMasterRepo extends JpaRepository<TradeFeedMasterEntity, Long> {
}