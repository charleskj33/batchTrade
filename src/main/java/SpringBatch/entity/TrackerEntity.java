package SpringBatch.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Component
@ToString
@Table(name="TRACKER")
public class TrackerEntity {

    @Id
    private Long Id;
    private String batchId;
    private String service;
    private String status;
    private LocalDateTime insertedTimeStamp;
    private LocalDateTime updatedTimeStamp;
    private String desc;
}
