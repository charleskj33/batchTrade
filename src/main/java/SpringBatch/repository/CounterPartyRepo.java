package SpringBatch.repository;

import SpringBatch.entity.CounterParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CounterPartyRepo extends JpaRepository<CounterParty, String> {

    @Query(value = "select cmId from cptys where counName=:cName AND clientCmId=:cCmid state='A'")
    Optional<String> findByCountName(@Param("cName") String cName, @Param("cCmid") String cCmid);
}
