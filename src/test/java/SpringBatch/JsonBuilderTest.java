package SpringBatch;
import SpringBatch.Util.JsonBuilder;
import SpringBatch.dto.TradeDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonBuilderTest {
    private final ObjectMapper mapper = new ObjectMapper();
    TradeDto tradeDto;

    @BeforeEach
    public void setUp() {
        tradeDto = new TradeDto();
        tradeDto.setPrincipal("XD3233");
        tradeDto.setTradeRef("54344");
        tradeDto.setCounterParty("USERE");
        tradeDto.setTradeRef2("MXSD43X");
        tradeDto.setMtmValuation("8585894.553");
        tradeDto.setNotional("50000");
        tradeDto.setStartDate("2025-06-04");
        tradeDto.setEndDate("2025-06-04");
        tradeDto.setDealDate("2025-06-04");
        tradeDto.setMtmValuationDate("2025-06-03");

        tradeDto.setMisc2("XD3233");
        tradeDto.setMisc4("rer443fff");
    }

        @Test
        void testBuildJson_withValidData_returnsCorrectJsonStructure() throws Exception {
            TradeDto tradeDto = new TradeDto();
            tradeDto.setPrincipal("ABC Corp");
            tradeDto.setCounterParty("XYZ Ltd");
            tradeDto.setTradeRef("TRD123");
            tradeDto.setMtmValuation("1000.50");
            tradeDto.setMtmValuationDate("2024-06-04");
            tradeDto.setStartDate("2024-01-01");
            tradeDto.setEndDate("2024-12-31");
            tradeDto.setDealDate("2024-01-15");

            String json = JsonBuilder.buildJson(tradeDto);
            JsonNode root = mapper.readTree(json);

            assertEquals("ABC Corp", root.path("principal").asText());
            assertEquals("XYZ Ltd", root.path("counterParty").asText());
            assertEquals("TRD123", root.path("tradeRef").asText());
            assertEquals("2024-06-04", root.path("valuationDate").asText());

            JsonNode tradeDates = root.path("tradeDates");
            assertEquals("2024-01-01", tradeDates.path("startDate").asText());
            assertEquals("2024-12-31", tradeDates.path("endDate").asText());
            assertEquals("2024-01-15", tradeDates.path("dealDate").asText());

            JsonNode mtms = root.path("mtms");
            assertTrue(mtms.isArray());
            assertEquals(1, mtms.size());

            JsonNode mtm = mtms.get(0);
            assertEquals("1000.50", mtm.path("amount").asText());
            assertEquals("USD", mtm.path("currency").asText());
            assertEquals("Principal", mtm.path("provider").asText());
        }

        @Test
        void testBuildJson_withEmptyFields_shouldOmitNullFields() throws Exception {
            TradeDto tradeDto = new TradeDto();
            tradeDto.setTradeRef("TRD999"); // only one field set

            String json = JsonBuilder.buildJson(tradeDto);
            JsonNode root = mapper.readTree(json);

            assertTrue(root.has("tradeRef"));
            assertFalse(root.has("principal"));
            assertFalse(root.has("mtms"));
            assertFalse(root.has("tradeDates"));
        }

        @Test
        void testBuildJson_withInvalidDate_shouldThrowException() {
            TradeDto tradeDto = new TradeDto();
            tradeDto.setStartDate("invalid-date");

            Exception exception = assertThrows(RuntimeException.class, () -> {
                JsonBuilder.buildJson(tradeDto);
            });

            assertTrue(exception.getMessage().contains("Invalid excep"));
        }

        @Test
        void testBuildJson_withInvalidMtm_shouldReturnZeroAmount() throws Exception {
            TradeDto tradeDto = new TradeDto();
            tradeDto.setMtmValuation("invalid");
            tradeDto.setTradeRef("TRD321");

            String json = JsonBuilder.buildJson(tradeDto);
            JsonNode root = mapper.readTree(json);

            JsonNode mtms = root.path("mtms");
            JsonNode mtm = mtms.get(0);

            assertEquals("invalid", mtm.path("amount").asText()); // still added as string
        }

}