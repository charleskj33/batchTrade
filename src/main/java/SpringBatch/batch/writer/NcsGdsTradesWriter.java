package SpringBatch.batch.writer;

import SpringBatch.Util.JsonBuilder;
import SpringBatch.dto.TradeDtoWrapper;
import SpringBatch.entity.TradeEntity;
import SpringBatch.service.NcsFeedDataService;
import SpringBatch.dto.FileMetadata;
import SpringBatch.dto.TradeDto;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class NcsGdsTradesWriter implements ItemWriter<TradeDtoWrapper> {

    private FileMetadata fileMetadata;
    private NcsFeedDataService tradeService;

    public NcsGdsTradesWriter(FileMetadata fileMetadata, NcsFeedDataService tradeService) {
        this.fileMetadata = fileMetadata;
        this.tradeService = tradeService;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void write(Chunk<? extends TradeDtoWrapper> chunk) throws Exception {
        int total = fileMetadata.getTotalRecords();

        log.info("total records {} ", total);
        List<TradeEntity> tradeEntities = new ArrayList<>();

            for (TradeDtoWrapper wrapper : chunk.getItems()) {
                try {
                    //fileMetadata.incrementProcessRecords(1);
                    TradeDto tradeDto = wrapper.getTradeDto();
                    String externalId = wrapper.getExternalId();
                    String clientName = wrapper.getClientName();
                    String json = JsonBuilder.buildJson(tradeDto);

                    String mode = tradeService.determineMode(clientName);
                    fileMetadata.incrementClientRecord(clientName);
                    fileMetadata.setClientModes(clientName, mode);

                    prepareNcsFeedData(json, tradeEntities, clientName, externalId);

                } catch (Exception e) {
                    log.error("Error while processing trade for Error: {}", e.getMessage(), e);
                }
            }

            // Persist per client (optional), or combine outside loop
            tradeService.persistMarketData(tradeEntities);

    }

    private void prepareNcsFeedData(String jsonOutput, List<TradeEntity> tradeEntityList, String clientName, String externalId){
        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.setBatchId(fileMetadata.getBatchIdForClient(clientName));
        tradeEntity.setFeedType("trades");
        tradeEntity.setJsonOutput(jsonOutput);
        tradeEntity.setSourceSystem("gds");
        tradeEntity.setExternalId(externalId);
        tradeEntity.setInsertedTimeStamp(LocalDateTime.now());
        tradeEntity.setClientName(clientName);
        tradeEntity.setMode(fileMetadata.getClientModes(clientName));
        tradeEntityList.add(tradeEntity);
    }
}
