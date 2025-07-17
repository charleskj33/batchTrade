package SpringBatch.batch.config;

import javax.sql.DataSource;

import SpringBatch.Util.TradeUtil;
import SpringBatch.batch.Processor.NcsGdsTradesProcessor;
import SpringBatch.batch.reader.NcsGdsTradesReader;
import SpringBatch.batch.writer.NcsGdsTradesWriter;
import SpringBatch.dto.FileMetadata;
import SpringBatch.dto.TradeDto;
import SpringBatch.dto.TradeDtoWrapper;
import SpringBatch.service.KafkaProducerService;
import SpringBatch.service.NcsFeedDataService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.*;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.core.task.*;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

	private final NcsGdsTradesReader itemReader;
	private final NcsGdsTradesProcessor itemProcessor;
	private final NcsGdsTradesWriter itemWriter;
	private final DataSource dataSource;
	private final FileMetadata fileMetadata;
	private final KafkaProducerService kafkaProducerService;
	private final NcsFeedDataService ncsFeedDataService;

	// Declares a step in the batch job
	@Bean
	public Step processStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("tradeBatchStep", jobRepository)
				.<TradeDto, TradeDtoWrapper>chunk(500, transactionManager)
				.reader(itemReader) // Reads each item
				.processor(itemProcessor) // Processes each item
				.writer(itemWriter) // Writes the processed item
				.taskExecutor(taskExecutor())
				.listener(stepExecutionListener())
				.build();
	}

	// Declares the batch job
	@Bean
	public Job processJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new JobBuilder("processTradeJob", jobRepository) // Starts with the defined step
				.start(processStep(jobRepository, transactionManager))
				.build();
	}

	// Returns the JobRepository
	@Bean
	public JobRepository jobRepository(PlatformTransactionManager transactionManager) throws Exception {
		var factory = new JobRepositoryFactoryBean();
		factory.setTransactionManager(transactionManager);
		factory.setDataSource(dataSource); // Injected DataSource
		factory.setDatabaseType("H2");
		factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
		factory.setTablePrefix("BATCH_"); // Prefix for Spring Batch metadata tables
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean
	public TaskExecutor taskExecutor() {
		var executor = new ThreadPoolTaskExecutor();
		int cores = Runtime.getRuntime().availableProcessors();
		executor.setCorePoolSize(cores * 2);
		executor.setMaxPoolSize(cores * 4);
		executor.setQueueCapacity(5000);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setThreadNamePrefix("batch-thread-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(60);
		executor.setThreadPriority(Thread.NORM_PRIORITY);
		executor.initialize();
		return executor;
	}

	private StepExecutionListener stepExecutionListener(){
		return new StepExecutionListener() {
			@Override
			public ExitStatus afterStep(StepExecution stepExecution) {
				boolean hasError = false;
				try {
					List<String> kafkaMessages = TradeUtil.prepareMetadata(fileMetadata);
					for(String kafkaMsg : kafkaMessages){
						kafkaProducerService.sendMessage(kafkaMsg);
					}
					ncsFeedDataService.persistExcepMarketData();
					ncsFeedDataService.persistFileLinking(fileMetadata);

				}catch (Exception e){
					e.printStackTrace();
				}

				boolean finalHasError = hasError;
				fileMetadata.getClientBatchIds().forEach((clientName, batchId) ->{
					String status = finalHasError ? "FAILED" : "COMPLETED";
					String des = finalHasError ? "Trade file process failed for client " : "Trade file process completed for client";
					ncsFeedDataService.updateTrackerStatus(batchId, status, des + clientName);
				});
				return ExitStatus.COMPLETED;
			}

		};
	}
}

