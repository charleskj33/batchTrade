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
public class Agreement {

    @Id
    @Column(name = "AgreementId")
    private String agreementId;

    @Column(name = "AgreementName")
    private String agreementName;

    @Column(name = "PrincipalCmId")
    private String principalCmId;

    @Column(name = "CptyCmId")
    private String cptyCmId;

    @Column(name = "State")
    private String state;

    @Column(name = "ClientCmId")
    private String clientCmId;

    @Column(name = "ExternalId")
    private String externalId;
}
