package SpringBatch.batch.Processor;

import SpringBatch.dto.FileMetadata;
import SpringBatch.dto.TradeDto;
import SpringBatch.dto.TradeDtoWrapper;
import SpringBatch.entity.*;
import SpringBatch.repository.AgreementRepo;
import SpringBatch.repository.ClientRepo;
import SpringBatch.repository.CounterPartyRepo;
import SpringBatch.repository.PrincipalRepo;
import SpringBatch.service.NcsFeedDataService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.Optional;

@Component
    @Slf4j
    public class NcsGdsTradesProcessor implements ItemProcessor<TradeDto, TradeDtoWrapper> {

        private final FileMetadata fileMetadata;
        private final PrincipalRepo principalRepo;
        private final CounterPartyRepo counterPartyRepo;
        private final AgreementRepo agreementRepo;
        private final ClientRepo clientRepo;
        private final NcsFeedDataService tradeService;

        @Autowired
        public NcsGdsTradesProcessor(FileMetadata fileMetadata, PrincipalRepo principalRepo, CounterPartyRepo counterPartyRepo, AgreementRepo agreementRepo, ClientRepo clientRepo, NcsFeedDataService tradeService) {
            this.fileMetadata = fileMetadata;
            this.principalRepo = principalRepo;
            this.counterPartyRepo = counterPartyRepo;
            this.agreementRepo = agreementRepo;

            this.clientRepo = clientRepo;
            this.tradeService = tradeService;
        }

    @Override
    public TradeDtoWrapper process(TradeDto item) {
        log.info("Processing tradeRef: {}", item.getTradeRef());

        try {
            String principalName = item.getPrincipal();
            String orgCode = item.getOrgCode();

            // Step 1: Find principal
            Principals principal =findPrincipalByName(principalName);

            if (principal == null) {
                return handleException(item, "Principal not found");
            }

            String clientName = findClientNameByCmId(principal.getClientCmId());

            if (clientName == null) {
                return handleException(item, "Client name not found");
            }

            // Step 3: Find counterparty using clientCmId
            String counterpartyId = findCounterPartyId(item.getOrgCode(), principal.getClientCmId());

            if (counterpartyId == null) {
                return handleException(item, "Counterparty not found");
            }

            // Step 4: Find agreement to get externalId
            String externalId = findExternalId(principal.getClientCmId(), counterpartyId);

            if (externalId == null) {
                return handleException(item, "Missing client name or externalId");
            }

            return new TradeDtoWrapper(item, clientName, externalId);

        } catch (Exception e) {
            log.error("Unexpected error while processing tradeRef {}: {}", item.getTradeRef(), e.getMessage(), e);
            return handleException(item, "Unexpected error while processing trade");
        }
    }

    // Centralized handler for known (business) failures
    private TradeDtoWrapper handleException(TradeDto item, String message) {
        feedDataExcepEntity(fileMetadata.getBatchId(), tradeService, item.getTradeRef(), message);
        log.warn("Business rule failed for tradeRef {}: {}", item.getTradeRef(), message);
        return null;
    }

    private static void feedDataExcepEntity(String batchId, NcsFeedDataService ncsFeedDataService, String tradeRef, String errMsg){
        ExceptionEntity entity = new ExceptionEntity();
        entity.setBatchId(batchId);
        entity.setTradeRef(tradeRef);
        entity.setErrorMessage(errMsg);
        ncsFeedDataService.addExcepAdd(entity);
    }

    private Principals findPrincipalByName(String principalName){
        if(StringUtils.isBlank(principalName)){
            return null;
        }
        return principalRepo.findByPcmByName(principalName)
                .orElse(null);
    }

    private String findClientNameByCmId(String clientCmId){
        if(StringUtils.isBlank(clientCmId)){
            return null;
        }
        return clientRepo.findByShortName(clientCmId)
                .map(Clients::getShortName)
                .orElse(null);
    }

    private String findCounterPartyId(String orgCode, String clientCmId){
        if(StringUtils.isBlank(orgCode) || StringUtils.isBlank(clientCmId)){
            return null;
        }

        return counterPartyRepo.findByCountName(orgCode, clientCmId)
                .orElse(null);
    }

    private String findExternalId(String principalCmId, String counterPartId){
        if(StringUtils.isBlank(principalCmId) || StringUtils.isBlank(counterPartId)){
            return null;
        }
        return agreementRepo.findByExternalId(principalCmId, counterPartId)
                .map(Agreement::getExternalId)
                .orElse(null);
    }
}

