package bg.fibank.cashdesk.common.utils.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyConfig {

    @Value("${security.api.key}")
    private String apiKey;

    @Value("${security.api.header}")
    private String apiKeyHeader;

    public String getApiKey() {
        return apiKey;
    }

    public String getApiKeyHeader() {
        return apiKeyHeader;
    }
}
