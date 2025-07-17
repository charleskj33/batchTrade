package SpringBatch.repository;


import SpringBatch.entity.ExceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepositoryException extends JpaRepository<ExceptionEntity, Long> {
}
