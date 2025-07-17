package SpringBatch;

import SpringBatch.batch.Processor.NcsGdsTradesProcessor;
import SpringBatch.dto.FileMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;

import static org.mockito.Mockito.*;

class NcsGdsTradesProcessorTest {

    @Mock
    private FileMetadata fileMetadata;
    private NcsGdsTradesProcessor processor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(fileMetadata.getBatchId()).thenReturn("batch-001");
    
        //processor = new NcsGdsTradesProcessor(fileMetadata, principalRepo, counterPartyRepo, agreementRepo, clientRepo);
    }

    
}