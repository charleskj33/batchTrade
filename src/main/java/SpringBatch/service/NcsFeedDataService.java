package SpringBatch.service;

import SpringBatch.dto.FileMetadata;
import SpringBatch.entity.ExceptionEntity;
import SpringBatch.entity.TrackerEntity;
import SpringBatch.entity.TradeEntity;
import SpringBatch.entity.TradeFeedMasterEntity;
import SpringBatch.repository.TrackerRepo;
import SpringBatch.repository.TradeMasterRepo;
import SpringBatch.repository.TradeRepository;
import SpringBatch.repository.TradeRepositoryException;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NcsFeedDataService {
    private final TradeRepository tradeRepository;
    private final TradeRepositoryException tradeRepositoryException;

    private final List<ExceptionEntity> exceptionEntityList = new ArrayList<>();
    private final TradeMasterRepo tradeMasterRepo;


    @PersistenceContext
    private EntityManager entityManager;

    private final JavaMailSender javaMailSender;

    private final TrackerRepo feedTrackerRepo;

    @Value("${spring.mail.username}")
    String mailName;

    public NcsFeedDataService(TradeRepository tradeRepository, TradeRepositoryException tradeRepositoryException, TradeMasterRepo tradeMasterRepo, JavaMailSender javaMailSender, TrackerRepo trackerRepo) {
        this.tradeRepository = tradeRepository;
        this.tradeRepositoryException = tradeRepositoryException;
        this.tradeMasterRepo = tradeMasterRepo;
        this.javaMailSender = javaMailSender;
        this.feedTrackerRepo = trackerRepo;
    }

    @Transactional()
    public void persistMarketData(List<TradeEntity> tradeEntity) {

        try{
            tradeRepository.saveAll(tradeEntity);
            entityManager.flush();
            entityManager.clear();

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void addExcepAdd(ExceptionEntity excep) {
        try{
            exceptionEntityList.add(excep);
        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }

    @Transactional()
    public void persistExcepMarketData() {
        try {
            List<List<ExceptionEntity>> batches = new ArrayList<>();
            for(int i =0 ; i<exceptionEntityList.size();i+=1000){
                int endIndex = Math.min(i + 1000, exceptionEntityList.size());
                batches.add(new ArrayList<>(exceptionEntityList.subList(i, endIndex)));
            }

            for(List<ExceptionEntity> batch : batches){
                tradeRepositoryException.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void sendEmailWithAttachment() throws MessagingException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(mailName);
            helper.setSubject("Action Needed: Invalid Data");
            helper.setText("unable to proceed with invalid data");
            javaMailSender.send(message);
        }catch (MessagingException e){
            e.printStackTrace();
        }
    }

    public void publishTracker(String batchId, String serviceName, String status, String description){
        if(feedTrackerRepo == null){
            log.error("Failed to save");
            return;
        }

        try{
            feedTrackerRepo.save(generateEntity(batchId, serviceName, status, description));
        }catch (Exception e){
            throw new RuntimeException();
        }
    }

    TrackerEntity generateEntity(String batchId, String serviceName, String status, String desc){
        TrackerEntity te = new TrackerEntity();
        te.setBatchId(batchId);
        te.setService(serviceName);
        te.setStatus(status);
        te.setDesc(desc);
        te.setInsertedTimeStamp(LocalDateTime.now());
        te.setUpdatedTimeStamp(LocalDateTime.now());
        return te;
    }

    public void updateTrackerStatus(String batchId, String status, String description){

        if(StringUtils.isEmpty(batchId)){
            throw new RuntimeException();
        }

        feedTrackerRepo.findByBatchId(batchId).ifPresentOrElse(tracker -> {
            tracker.setStatus(status);
            tracker.setDesc(description);
            tracker.setUpdatedTimeStamp(LocalDateTime.now());
            feedTrackerRepo.save(tracker);
        }, () -> {
            log.warn("No Tracker found for batchId {}. Skipping update.", batchId);
        });
    }

    public String determineMode(String clientName){
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        boolean existsToday = tradeRepository.existsForCLientToday(clientName, "trades", startOfDay, endOfDay);

        return existsToday ? "Delta" : "Flush";
    }

    public void persistFileLinking(FileMetadata fileMetadata) {
        try {
            List<TradeFeedMasterEntity> links = fileMetadata.getClientBatchIds().entrySet().stream()
                    .map(entry -> TradeFeedMasterEntity.builder()
                            .fileId(fileMetadata.getBatchId())
                            .clientName(entry.getKey())
                            .batchId(entry.getValue())
                            .sourceSystem(fileMetadata.getSourceSystem())
                            .totalMsg(fileMetadata.getClientRecordCount(entry.getKey()))
                            .insertedTimeStamp(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());

            tradeMasterRepo.saveAll(links);
        } catch (Exception e) {
            log.error("Error saving file linking records", e);
            throw new RuntimeException("Error persisting file linking data", e);
        }
    }

}


    /*@Transactional()
    public void persistMarketData(List<TradeEntity> tradeEntity) {
        if (tradeEntity == null || tradeEntity.isEmpty()) {
            return;
        }
        // Assume all trades in the list have the same batchId and service (if not, adapt as needed)
        String batchId = tradeEntity.get(0).getBatchId();
        String serviceName = "TradePersist"; // Or get from context/param if needed
        String trackerDesc = "Processing trades for batchId: " + batchId;
        TrackerEntity tracker = null;
        try {
            // Insert INPROGRESS tracker
            tracker = feedTrackerRepo.save(generateEntity(batchId, serviceName, "INPROGRESS", trackerDesc));
            tradeRepository.saveAll(tradeEntity);
            entityManager.flush();
            entityManager.clear();
            // Update tracker to COMPLETED
            if (tracker != null) {
                tracker.setStatus("COMPLETED");
                tracker.setUpdatedTimeStamp(java.time.LocalDateTime.now());
                feedTrackerRepo.save(tracker);
            }
        } catch (Exception e) {
            // Update tracker to ERROR
            if (tracker != null) {
                tracker.setStatus("ERROR");
                tracker.setDesc(trackerDesc + ", error: " + e.getMessage());
                tracker.setUpdatedTimeStamp(java.time.LocalDateTime.now());
                feedTrackerRepo.save(tracker);
            } else {
                // If tracker insert failed, try to log error tracker
                try {
                    feedTrackerRepo.save(generateEntity(batchId, serviceName, "ERROR", trackerDesc + ", error: " + e.getMessage()));
                } catch (Exception ex) {
                    log.error("Failed to log tracker error: {}", ex.getMessage());
                }
            }
            throw new RuntimeException(e);
        }
    }
*/
