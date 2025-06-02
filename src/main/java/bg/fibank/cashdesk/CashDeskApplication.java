package bg.fibank.cashdesk;

import bg.fibank.cashdesk.common.utils.config.ApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApiProperties.class)
public class CashDeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashDeskApplication.class, args);
    }
}

