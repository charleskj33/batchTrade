package SpringBatch.batch.config;

import SpringBatch.repository.ClientRepo;
import SpringBatch.repository.PrincipalRepo;
import SpringBatch.service.NcsFeedDataService;
import SpringBatch.batch.reader.NcsGdsTradesReader;
import SpringBatch.dto.FileMetadata;
import SpringBatch.dto.TradeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.lang.reflect.Field;
import java.util.Arrays;

@Configuration
@Slf4j
public class ReaderConfig {

    @Bean
    @StepScope
    @Qualifier("flatFileItemReader")
    public FlatFileItemReader<TradeDto> flatFileItemReader(LineMapper<TradeDto> lineMapper,
                                                           @Value("trade.csv") String inputFilePath,
                                                           FileMetadata fileMetadata) {
        FlatFileItemReader<TradeDto> reader = new FlatFileItemReader<>();
        try {
            FileSystemResource resource = new FileSystemResource(inputFilePath);
            if(!resource.exists()){
                throw new IllegalStateException("File not Found"+ resource.getPath());
            }
            reader.setResource(resource);
            reader.setLinesToSkip(1);
            reader.setLineMapper(lineMapper);

            fileMetadata.setSourceSystem("GDS");
        }catch (IllegalStateException e){
            log.error("error occured", e);
        }
        return reader;
    }

    private String[] getPoJoFieldNames(Class<?> clazz){
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .toArray(String[]::new);
    }
    @Bean
    @StepScope
    public NcsGdsTradesReader ncsGdsTradesReader(@Qualifier("flatFileItemReader") FlatFileItemReader<TradeDto> delegate, FileMetadata fileMetadata, NcsFeedDataService tradeService,
                                                 PrincipalRepo principalRepo, ClientRepo clientRepo) throws Exception {
        return new NcsGdsTradesReader(delegate, fileMetadata, tradeService, principalRepo, clientRepo);
    }

    @Bean
    private LineMapper<TradeDto> lineMapper() {
        DefaultLineMapper<TradeDto> lineMapper = new DefaultLineMapper<>();
        int expectedFields = 6;

        try {

            String[] fieldNames = getPoJoFieldNames(TradeDto.class);
            DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
            lineTokenizer.setNames(fieldNames);
            lineTokenizer.setStrict(true);

            BeanWrapperFieldSetMapper<TradeDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
            fieldSetMapper.setTargetType(TradeDto.class);

            lineMapper.setLineTokenizer(lineTokenizer);
            lineMapper.setFieldSetMapper(fieldSet -> {
                log.info("field count {}", fieldSet.getFieldCount());
                if (fieldSet.getFieldCount() != expectedFields) {
                    log.error("invalid count");
                    return null;
                }
                return fieldSetMapper.mapFieldSet(fieldSet);
            });
        }catch (Exception e){
            log.error("Exception Occurred", e);
            throw e;
        }

        return lineMapper;
    }
}
