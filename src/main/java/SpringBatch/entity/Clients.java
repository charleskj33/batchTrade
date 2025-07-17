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
@Table(name="AGREEMENT")
public class Clients {

    @Id
    @Column(name = "ClientCmId")
    private String clientCmId;

    @Column(name = "ShortName")
    private String shortName;

    @Column(name = "LongName")
    private String longName;

    @Column(name = "State")
    private String state;
}
