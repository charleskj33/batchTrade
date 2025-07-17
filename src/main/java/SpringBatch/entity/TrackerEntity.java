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
@Table(name="FEED_TRACKER")
public class TrackerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String batchId;
    private String service;
    private String status;
    private LocalDateTime insertedTimeStamp;
    private LocalDateTime updatedTimeStamp;
    private String desc;
}
