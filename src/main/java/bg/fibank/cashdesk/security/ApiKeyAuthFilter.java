package bg.fibank.cashdesk.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends HttpFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    @Value("${security.api.key}")
    private String apiKey;

    @Value("${security.api.header}")
    private String apiKeyHeader;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String headerValue = request.getHeader(apiKeyHeader);

        logger.info("API Key Header Name: {}", apiKeyHeader);
        logger.info("Received API Key: {}", headerValue);
        logger.info("Expected API Key: {}", apiKey);

        if (apiKey != null && apiKey.equals(headerValue)) {
            logger.info("API key matched, proceeding with request.");
            chain.doFilter(request, response);
        } else {
            logger.warn("API key missing or invalid. Rejecting request.");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized: Missing or invalid API key");
        }
    }
}
