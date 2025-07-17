package SpringBatch.repository;

import SpringBatch.entity.Principals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrincipalRepo extends JpaRepository<Principals, String> {

    @Query(value = "SELECT * from REF_PRINCIPALS where lookup=:pName AND state = 'A'", nativeQuery = true)
    Optional<Principals> findByPcmByName(@Param("pName") String pName);
}
