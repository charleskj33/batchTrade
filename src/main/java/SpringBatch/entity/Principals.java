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
@Table(name="PRINCIPALS")
public class Principals {

    @Id
    @Column(name = "PrincipalCmId")
    private String principalCmId;

    @Column(name = "PrincipalName")
    private String principalName;

    @Column(name = "State")
    private String state;

    @Column(name = "ClientCmId")
    private String clientCmId;

}
