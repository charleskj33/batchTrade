package SpringBatch.entity;

import jakarta.persistence.Column;
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
@Table(name="CPTYS")
public class CounterParty {

    @Id
    @Column(name = "CounterPartyCmId")
    private String counterPartyCmId;

    @Column(name = "CounterPartyName")
    private String counterPartyName;

    @Column(name = "State")
    private String state;

    @Column(name = "ClientCmId")
    private String clientCmId;
}
