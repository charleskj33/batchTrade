/*
package SpringBatch.controller;

import SpringBatch.blobStorage.EventGridEvent;
import SpringBatch.blobStorage.StorageBlobCreatedEventData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
    @RequestMapping("/events")
    @Slf4j
public class EventGridController {

        private final ObjectMapper objectMapper;
        private final JobLauncher jobLauncher;
        private final Job processJob; // Inject your Spring Batch Job

    public EventGridController(ObjectMapper objectMapper, JobLauncher jobLauncher, Job processJob) {
            this.objectMapper = objectMapper;
            this.jobLauncher = jobLauncher;
            this.processJob = processJob;
        }

        @PostMapping("/blob-created")
        public ResponseEntity<String> handleEventGridEvent(@RequestBody List<EventGridEvent> events) {
        log.info("Received Event Grid events: {}", events);

        for (EventGridEvent event : events) {
            // Handle Event Grid Subscription Validation
            if ("Microsoft.EventGrid.SubscriptionValidationEvent".equals(event.getEventType())) {
                String validationCode = event.getData().get("validationCode").asText();
                log.info("Event Grid validation event received. Validation Code: {}", validationCode);
                return ResponseEntity.ok("{\"validationResponse\": \"" + validationCode + "\"}");
            }

            // Handle Blob Created Event
            if ("Microsoft.Storage.BlobCreated".equals(event.getEventType())) {
                try {
                    StorageBlobCreatedEventData data = objectMapper.treeToValue(event.getData(), StorageBlobCreatedEventData.class);
                    String blobUrl = data.getUrl();
                    String filename = extractFilenameFromUrl(blobUrl);

                    log.info("Blob Created Event: URL = {}, Filename = {}", blobUrl, filename);

                    // Trigger the Spring Batch job with the blob URL as a parameter
                    JobParameters jobParameters = new JobParametersBuilder()
                            .addString("filePath", blobUrl)
                            .addLong("time", System.currentTimeMillis()) // Unique parameter for job restartability
                            .toJobParameters();

                    jobLauncher.run(processJob, jobParameters);
                    log.info("Spring Batch job triggered for file: {}", blobUrl);

                } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
                    log.error("Error launching Spring Batch job for blob {}: {}", event.getSubject(), e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Job already running or invalid parameters for " + event.getSubject());
                } catch (Exception e) {
                    log.error("Error processing BlobCreated event for blob {}: {}", event.getSubject(), e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing event for " + event.getSubject());
                }
            }
        }

        return ResponseEntity.ok("Events processed successfully");
    }

    private String extractFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        }
        return url; // Return full URL if no slash found or it's the last char
    }
}
*/
