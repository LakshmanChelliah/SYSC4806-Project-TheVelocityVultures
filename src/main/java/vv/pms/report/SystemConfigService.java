package vv.pms.report;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.report.internal.SystemConfigRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Transactional
public class SystemConfigService {

    private static final String REPORT_DEADLINE_KEY = "REPORT_DEADLINE";
    private final SystemConfigRepository repository;

    public SystemConfigService(SystemConfigRepository repository) {
        this.repository = repository;
    }

    public void setReportDeadline(LocalDateTime deadline) {
        String value = deadline.format(DateTimeFormatter.ISO_DATE_TIME);
        SystemConfig config = repository.findByConfigKey(REPORT_DEADLINE_KEY)
                .orElse(new SystemConfig(REPORT_DEADLINE_KEY, value));
        config.setConfigValue(value);
        repository.save(config);
    }

    @Transactional(readOnly = true)
    public Optional<LocalDateTime> getReportDeadline() {
        return repository.findByConfigKey(REPORT_DEADLINE_KEY)
                .map(config -> LocalDateTime.parse(config.getConfigValue(), DateTimeFormatter.ISO_DATE_TIME));
    }

    @Transactional(readOnly = true)
    public boolean isBeforeDeadline() {
        return getReportDeadline()
                .map(deadline -> LocalDateTime.now().isBefore(deadline))
                .orElse(true); // If no deadline set, assume it's open (or handle as needed, but usually no deadline means no restriction)
    }
}
