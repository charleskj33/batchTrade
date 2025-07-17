package SpringBatch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Component
@ToString
@Entity
public class TradeEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column
        private String batchId;
        @Column
        private String sourceSystem;
        @Column
        private String feedType;
        @Column
        private String jsonOutput;

        @Column
        private String externalId;
        @Column
        private LocalDateTime insertedTimeStamp;
        @Column
        private String clientName;
        @Column
        private String mode;
}
