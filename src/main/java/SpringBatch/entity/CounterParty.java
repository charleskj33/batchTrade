package SpringBatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Component
@ToString
@Entity
@Table(name="REF_CPTYS")
public class CounterParty {

    @Id
    private String cmId;
    private String name;
    private String state;
    private String country;
    private String clientCmId;
    private String lookup;
}
