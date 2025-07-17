package SpringBatch.dto;

import lombok.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Component
@ToString
public class TradeDtoWrapper {
    private TradeDto tradeDto;
    private String clientName;
    private String externalId;
}
