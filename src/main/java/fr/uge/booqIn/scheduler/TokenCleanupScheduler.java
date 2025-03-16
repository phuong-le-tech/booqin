package fr.uge.booqIn.scheduler;

import fr.uge.booqIn.service.JwtService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;
@Component
@EnableScheduling
public class TokenCleanupScheduler {
    
    private static final Logger logger = Logger.getLogger(TokenCleanupScheduler.class.getName());
    
    private final JwtService jwtService;
    
    public TokenCleanupScheduler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupExpiredTokens() {
        logger.info("Running scheduled token cleanup task");
        jwtService.cleanupExpiredTokens();
    }
}