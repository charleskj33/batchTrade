package SpringBatch.dto;


import lombok.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Component
@ToString
public class TradeDto {

    private String principal;
    private String counterParty;
    private String system;
    private String product;
    private String orgCode;
    private String tradeRef;
    private String tradeRef2;
    private String dealDate;
    private String startDate;
    private String endDate;
    private String tradeAction;
    private String notional;
    private String currency;
    private String mtmValuation;
    private String mtmValuationDate;
    private String mtmValuationTradeCcy;
    private String misc2;
    private String misc4;
}
