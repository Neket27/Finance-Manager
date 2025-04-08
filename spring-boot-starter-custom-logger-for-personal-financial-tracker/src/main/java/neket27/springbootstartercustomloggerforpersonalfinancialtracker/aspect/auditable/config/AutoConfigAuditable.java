package neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.auditable.config;

import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.auditable.AuditAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class AutoConfigAuditable {

    @Bean
    public AuditAspect auditAspect() {
        return new AuditAspect();
    }

}
