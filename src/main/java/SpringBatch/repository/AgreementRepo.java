package SpringBatch.repository;

import SpringBatch.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgreementRepo extends JpaRepository<Agreement, String> {

    @Query(
            value = "SELECT * FROM REF_AGREEMENT WHERE principalCmId = :pCmId AND cptyCmId = :cCmId AND state = 'A'",
            nativeQuery = true
    )
    Optional<Agreement> findByExternalId(@Param("pCmId") String pCmId, @Param("cCmId") String cCmId);
}

