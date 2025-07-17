package SpringBatch.repository;


import SpringBatch.entity.ExceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepositoryException extends JpaRepository<ExceptionEntity, Long> {
}
