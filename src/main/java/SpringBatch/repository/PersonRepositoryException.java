package SpringBatch.repository;

import SpringBatch.dto.TradeDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepositoryException extends JpaRepository<TradeDto, Long> {
}
