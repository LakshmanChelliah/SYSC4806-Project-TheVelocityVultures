package vv.pms.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vv.pms.report.internal.SystemConfigRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceTest {

    @Mock
    private SystemConfigRepository repository;

    @InjectMocks
    private SystemConfigService service;

    @Test
    void setReportDeadline_savesConfig() {
        LocalDateTime deadline = LocalDateTime.now().plusDays(1);
        when(repository.findByConfigKey("REPORT_DEADLINE")).thenReturn(Optional.empty());
        
        service.setReportDeadline(deadline);
        
        verify(repository).save(any(SystemConfig.class));
    }

    @Test
    void getReportDeadline_returnsParsedDateTime() {
        LocalDateTime now = LocalDateTime.now();
        // Truncate to seconds/minutes to avoid precision issues during string roundtrip if needed, 
        // but ISO_DATE_TIME usually handles it.
        String iso = now.format(DateTimeFormatter.ISO_DATE_TIME);
        SystemConfig config = new SystemConfig("REPORT_DEADLINE", iso);
        when(repository.findByConfigKey("REPORT_DEADLINE")).thenReturn(Optional.of(config));

        Optional<LocalDateTime> result = service.getReportDeadline();

        assertTrue(result.isPresent());
        assertEquals(iso, result.get().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    @Test
    void isBeforeDeadline_returnsTrue_whenDeadlineInFuture() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        String iso = future.format(DateTimeFormatter.ISO_DATE_TIME);
        SystemConfig config = new SystemConfig("REPORT_DEADLINE", iso);
        when(repository.findByConfigKey("REPORT_DEADLINE")).thenReturn(Optional.of(config));

        assertTrue(service.isBeforeDeadline());
    }

    @Test
    void isBeforeDeadline_returnsFalse_whenDeadlineInPast() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        String iso = past.format(DateTimeFormatter.ISO_DATE_TIME);
        SystemConfig config = new SystemConfig("REPORT_DEADLINE", iso);
        when(repository.findByConfigKey("REPORT_DEADLINE")).thenReturn(Optional.of(config));

        assertFalse(service.isBeforeDeadline());
    }

    @Test
    void isBeforeDeadline_returnsTrue_whenNoDeadlineSet() {
        when(repository.findByConfigKey("REPORT_DEADLINE")).thenReturn(Optional.empty());
        assertTrue(service.isBeforeDeadline());
    }
}
