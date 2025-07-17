package SpringBatch.repository;

import SpringBatch.entity.Clients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepo extends JpaRepository<Clients, String> {

    @Query(value = "SELECT * FROM REF_CLIENTS WHERE cmId = :cCmId AND state = 'A'", nativeQuery = true)
    Optional<Clients> findByShortName(@Param("cCmId") String cCmId);

}

