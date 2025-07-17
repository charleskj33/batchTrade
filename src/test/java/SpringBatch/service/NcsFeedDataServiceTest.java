package SpringBatch.service;

import SpringBatch.entity.ExceptionEntity;
import SpringBatch.entity.TrackerEntity;
import SpringBatch.entity.TradeEntity;
import SpringBatch.repository.TradeMasterRepo;
import SpringBatch.repository.TrackerRepo;
import SpringBatch.repository.TradeMasterRepo;
import SpringBatch.repository.TradeRepository;
import SpringBatch.repository.TradeRepositoryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NcsFeedDataServiceTest {
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private TradeRepositoryException tradeRepositoryException;
    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private TrackerRepo trackerRepo;
    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private NcsFeedDataService ncsFeedDataService;

    @Mock
    private TradeMasterRepo tradeMasterRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ncsFeedDataService = new NcsFeedDataService(tradeRepository, tradeRepositoryException, tradeMasterRepo, javaMailSender, trackerRepo);
    }

    @Test
    void testPublishTracker_Positive() {
        when(trackerRepo.save(any(TrackerEntity.class))).thenReturn(new TrackerEntity());
        assertDoesNotThrow(() -> ncsFeedDataService.publishTracker("batch1", "service1", "SUCCESS", "desc"));
        verify(trackerRepo, times(1)).save(any(TrackerEntity.class));
    }

    @Test
    void testPublishTracker_Negative_NullRepo() {
        NcsFeedDataService service = new NcsFeedDataService(tradeRepository, tradeRepositoryException, tradeMasterRepo, javaMailSender, null);
        service.publishTracker("batch1", "service1", "SUCCESS", "desc");
        // Should log error and return, not throw
    }

    @Test
    void testPublishTracker_Negative_Exception() {
        when(trackerRepo.save(any(TrackerEntity.class))).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> ncsFeedDataService.publishTracker("batch1", "service1", "FAIL", "desc"));
    }

    @Test
    void testUpdateTrackerStatus_Positive() {
        TrackerEntity tracker = new TrackerEntity();
        when(trackerRepo.findByBatchId("batch1")).thenReturn(Optional.of(tracker));
        when(trackerRepo.save(any(TrackerEntity.class))).thenReturn(tracker);
        assertDoesNotThrow(() -> ncsFeedDataService.updateTrackerStatus("batch1", "COMPLETED", "done"));
        verify(trackerRepo, times(1)).save(tracker);
    }

    @Test
    void testUpdateTrackerStatus_Negative_EmptyBatchId() {
        assertThrows(RuntimeException.class, () -> ncsFeedDataService.updateTrackerStatus("", "COMPLETED", "done"));
    }

    @Test
    void testUpdateTrackerStatus_Negative_NoTrackerFound() {
        when(trackerRepo.findByBatchId("batch1")).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> ncsFeedDataService.updateTrackerStatus("batch1", "FAILED", "not found"));
    }

    @Test
    void testDetermineMode_Positive_Delta() {
        when(tradeRepository.existsForCLientToday(anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(true);
        String mode = ncsFeedDataService.determineMode("client1");
        assertEquals("Delta", mode);
    }

    @Test
    void testDetermineMode_Positive_Flush() {
        when(tradeRepository.existsForCLientToday(anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(false);
        String mode = ncsFeedDataService.determineMode("client1");
        assertEquals("Flush", mode);
    }

    @Test
    void testPersistMarketData_Positive() {
        List<TradeEntity> trades = new ArrayList<>();
        trades.add(new TradeEntity());
        when(tradeRepository.saveAll(anyList())).thenReturn(trades);
        assertDoesNotThrow(() -> ncsFeedDataService.persistMarketData(trades));
    }

    @Test
    void testPersistMarketData_Negative_Exception() {
        List<TradeEntity> trades = new ArrayList<>();
        trades.add(new TradeEntity());
        when(tradeRepository.saveAll(anyList())).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> ncsFeedDataService.persistMarketData(trades));
    }

    @Test
    void testAddExcepAdd_Positive() {
        ExceptionEntity ex = new ExceptionEntity();
        assertDoesNotThrow(() -> ncsFeedDataService.addExcepAdd(ex));
    }

    @Test
    void testPersistExcepMarketData_Positive() {
        ExceptionEntity ex = new ExceptionEntity();
        ncsFeedDataService.addExcepAdd(ex);
        when(tradeRepositoryException.saveAll(anyList())).thenReturn(List.of(ex));
        assertDoesNotThrow(() -> ncsFeedDataService.persistExcepMarketData());
    }

    @Test
    void testPersistExcepMarketData_Negative_Exception() {
        ExceptionEntity ex = new ExceptionEntity();
        ncsFeedDataService.addExcepAdd(ex);
        when(tradeRepositoryException.saveAll(anyList())).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> ncsFeedDataService.persistExcepMarketData());
    }

    @Test
    void testSendEmailWithAttachment_Positive() throws Exception {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        assertDoesNotThrow(() -> ncsFeedDataService.sendEmailWithAttachment());
    }

    @Test
    void testSendEmailWithAttachment_Negative_MessagingException() throws Exception {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MessagingException("Mail error")).when(javaMailSender).send(any(MimeMessage.class));
        assertDoesNotThrow(() -> ncsFeedDataService.sendEmailWithAttachment()); // Should catch and print stack trace
    }
}
