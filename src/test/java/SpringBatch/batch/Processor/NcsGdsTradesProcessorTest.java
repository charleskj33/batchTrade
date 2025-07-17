package SpringBatch.batch.Processor;

import SpringBatch.dto.FileMetadata;
import SpringBatch.dto.TradeDto;
import SpringBatch.dto.TradeDtoWrapper;
import SpringBatch.entity.Agreement;
import SpringBatch.entity.Clients;
import SpringBatch.entity.Principals;
import SpringBatch.repository.AgreementRepo;
import SpringBatch.repository.ClientRepo;
import SpringBatch.repository.CounterPartyRepo;
import SpringBatch.repository.PrincipalRepo;
import SpringBatch.service.NcsFeedDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*class NcsGdsTradesProcessorTest {
    @Mock
    private FileMetadata fileMetadata;
    @Mock
    private PrincipalRepo principalRepo;
    @Mock
    private CounterPartyRepo counterPartyRepo;
    @Mock
    private AgreementRepo agreementRepo;
    @Mock
    private ClientRepo clientRepo;
    @Mock
    private NcsFeedDataService tradeService;

    @InjectMocks
    private NcsGdsTradesProcessor processor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processor = new NcsGdsTradesProcessor(fileMetadata, principalRepo, counterPartyRepo, agreementRepo, clientRepo, tradeService);
    }

    @Test
    void testProcess_PositiveCase_AllDataPresent() {
        TradeDto tradeDto = mock(TradeDto.class);
        when(tradeDto.getPrincipal()).thenReturn("principal");
        when(tradeDto.getOrgCode()).thenReturn("orgCode");
        when(tradeDto.getTradeRef()).thenReturn("tradeRef");

        Principals principals = mock(Principals.class);
        when(principalRepo.findByPcmByName("principal")).thenReturn(Optional.of(principals));
        when(principals.getClientCmId()).thenReturn("clientCmId");
        when(principals.getPrincipalCmId()).thenReturn("pcmId");

        Clients client = mock(Clients.class);
        when(clientRepo.findByShortName("clientCmId")).thenReturn(Optional.of(client));
        when(client.getShortName()).thenReturn("clientName");

        when(counterPartyRepo.findByCountName("orgCode", "clientCmId")).thenReturn(Optional.of("cId"));

        Agreement agreement = mock(Agreement.class);
        when(agreementRepo.findByExternalId("pcmId", "cId")).thenReturn(Optional.of(agreement));
        when(agreement.getExternalId()).thenReturn("externalId");

        TradeDtoWrapper result = processor.process(tradeDto);

        assertNotNull(result);
        assertEquals("clientName", result.getClientName());
        assertEquals("externalId", result.getExternalId());
    }

    @Test
    void testProcess_NegativeCase_PrincipalNotFound() {
        TradeDto tradeDto = mock(TradeDto.class);
        when(tradeDto.getPrincipal()).thenReturn("principal");
        when(tradeDto.getOrgCode()).thenReturn("orgCode");
        when(tradeDto.getTradeRef()).thenReturn("tradeRef");

        when(principalRepo.findByPcmByName("principal")).thenReturn(Optional.empty());

        TradeDtoWrapper result = processor.process(tradeDto);

        assertNull(result);
    }

    @Test
    void testProcess_NegativeCase_CounterpartyNotFound() {
        TradeDto tradeDto = mock(TradeDto.class);
        when(tradeDto.getPrincipal()).thenReturn("principal");
        when(tradeDto.getOrgCode()).thenReturn("orgCode");
        when(tradeDto.getTradeRef()).thenReturn("tradeRef");

        Principals principals = mock(Principals.class);
        when(principalRepo.findByPcmByName("principal")).thenReturn(Optional.of(principals));
        when(principals.getClientCmId()).thenReturn("clientCmId");
        when(principals.getPrincipalCmId()).thenReturn("pcmId");

        Clients client = mock(Clients.class);
        when(clientRepo.findByShortName("clientCmId")).thenReturn(Optional.of(client));
        when(client.getShortName()).thenReturn("clientName");

        when(counterPartyRepo.findByCountName("orgCode", "clientCmId")).thenReturn(Optional.empty());

        TradeDtoWrapper result = processor.process(tradeDto);

        assertNull(result);
    }

    @Test
    void testProcess_NegativeCase_AgreementNotFound() {
        TradeDto tradeDto = mock(TradeDto.class);
        when(tradeDto.getPrincipal()).thenReturn("principal");
        when(tradeDto.getOrgCode()).thenReturn("orgCode");
        when(tradeDto.getTradeRef()).thenReturn("tradeRef");

        Principals principals = mock(Principals.class);
        when(principalRepo.findByPcmByName("principal")).thenReturn(Optional.of(principals));
        when(principals.getClientCmId()).thenReturn("clientCmId");
        when(principals.getPrincipalCmId()).thenReturn("pcmId");

        Clients client = mock(Clients.class);
        when(clientRepo.findByShortName("clientCmId")).thenReturn(Optional.of(client));
        when(client.getShortName()).thenReturn("clientName");

        when(counterPartyRepo.findByCountName("orgCode", "clientCmId")).thenReturn(Optional.of("cId"));
        when(agreementRepo.findByExternalId("pcmId", "cId")).thenReturn(Optional.empty());

        TradeDtoWrapper result = processor.process(tradeDto);

        assertNull(result);
    }

    @Test
    void testProcess_NegativeCase_ClientNotFound() {
        TradeDto tradeDto = mock(TradeDto.class);
        when(tradeDto.getPrincipal()).thenReturn("principal");
        when(tradeDto.getOrgCode()).thenReturn("orgCode");
        when(tradeDto.getTradeRef()).thenReturn("tradeRef");

        Principals principals = mock(Principals.class);
        when(principalRepo.findByPcmByName("principal")).thenReturn(Optional.of(principals));
        when(principals.getClientCmId()).thenReturn("clientCmId");
        when(principals.getPrincipalCmId()).thenReturn("pcmId");

        when(clientRepo.findByShortName("clientCmId")).thenReturn(Optional.empty());

        when(counterPartyRepo.findByCountName("orgCode", "clientCmId")).thenReturn(Optional.of("cId"));

        Agreement agreement = mock(Agreement.class);
        when(agreementRepo.findByExternalId("pcmId", "cId")).thenReturn(Optional.of(agreement));
        when(agreement.getExternalId()).thenReturn("externalId");

        TradeDtoWrapper result = processor.process(tradeDto);

        assertNull(result);
    }

    @Test
    void testProcess_NegativeCase_MissingClientNameOrExternalId() {
        TradeDto tradeDto = mock(TradeDto.class);
        when(tradeDto.getPrincipal()).thenReturn("principal");
        when(tradeDto.getOrgCode()).thenReturn("orgCode");
        when(tradeDto.getTradeRef()).thenReturn("tradeRef");

        Principals principals = mock(Principals.class);
        when(principalRepo.findByPcmByName("principal")).thenReturn(Optional.of(principals));
        when(principals.getClientCmId()).thenReturn("clientCmId");
        when(principals.getPrincipalCmId()).thenReturn("pcmId");

        // clientRepo returns client with null shortName
        Clients client = mock(Clients.class);
        when(clientRepo.findByShortName("clientCmId")).thenReturn(Optional.of(client));
        when(client.getShortName()).thenReturn(null);

        when(counterPartyRepo.findByCountName("orgCode", "clientCmId")).thenReturn(Optional.of("cId"));

        Agreement agreement = mock(Agreement.class);
        when(agreementRepo.findByExternalId("pcmId", "cId")).thenReturn(Optional.of(agreement));
        when(agreement.getExternalId()).thenReturn(null);

        TradeDtoWrapper result = processor.process(tradeDto);

        assertNull(result);
    }

    @Test
    void testProcess_ExceptionThrown() {
        TradeDto tradeDto = mock(TradeDto.class);
        when(tradeDto.getPrincipal()).thenThrow(new RuntimeException("error"));
        when(tradeDto.getTradeRef()).thenReturn("tradeRef");
        when(fileMetadata.getBatchId()).thenReturn("batchId");

        TradeDtoWrapper result = processor.process(tradeDto);

        assertNull(result);
    }
}*/
