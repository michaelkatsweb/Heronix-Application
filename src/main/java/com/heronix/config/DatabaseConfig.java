package com.heronix.config;

import com.heronix.security.HeronixEncryptionService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Database Configuration with Heronix Encryption Support
 * Location: src/main/java/com/heronix/config/DatabaseConfig.java
 *
 * Configures HikariCP connection pools for different environments:
 * - dev: H2 embedded database (encrypted via CIPHER=AES)
 * - prod: PostgreSQL or H2 file-based
 *
 * H2 file databases are automatically encrypted with the Heronix master key.
 * The file password is derived from HERONIX_MASTER_KEY via PBKDF2.
 */
@Slf4j
@Configuration
public class DatabaseConfig {

    /**
     * Development profile: H2 embedded database with encryption
     */
    @Bean
    @Profile("dev")
    public DataSource devDataSource(
            @Value("${spring.datasource.username:sa}") String username,
            @Value("${spring.datasource.password:}") String password) {
        HikariConfig config = new HikariConfig();
        String url = "jdbc:h2:file:./data/heronix_dev;AUTO_SERVER=TRUE";
        applyH2Encryption(config, url, username, password);
        config.setDriverClassName("org.h2.Driver");

        // Dev pool settings - smaller pool
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }

    /**
     * Production profile: Uses environment variables for security
     * Supports PostgreSQL or H2 file-based
     */
    @Bean
    @Profile("prod")
    public DataSource prodDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driverClassName) {

        HikariConfig config = new HikariConfig();

        if (url.startsWith("jdbc:h2:file:")) {
            applyH2Encryption(config, url, username, password);
        } else {
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
        }
        config.setDriverClassName(driverClassName);

        // Production pool settings - larger pool, longer timeouts
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes

        // Performance tuning
        config.setAutoCommit(false);
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    /**
     * Test profile: In-memory H2 database (no file encryption for in-memory)
     */
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);

        return new HikariDataSource(config);
    }

    /**
     * Apply H2 CIPHER=AES encryption to a file-based H2 URL.
     * H2 requires the password format: "filePassword userPassword"
     */
    private void applyH2Encryption(HikariConfig config, String url, String username, String userPassword) {
        HeronixEncryptionService enc = HeronixEncryptionService.getInstance();
        if (enc.isDisabled()) {
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(userPassword);
            log.info("H2 encryption DISABLED (dev mode) — database is unencrypted");
            return;
        }

        // Add CIPHER=AES to URL if not already present
        if (!url.contains("CIPHER=AES")) {
            url = url + ";CIPHER=AES";
        }
        config.setJdbcUrl(url);
        config.setUsername(username);

        // H2 CIPHER=AES requires password format: "filePassword userPassword"
        String filePassword = enc.getH2FilePassword();
        String combinedPassword = filePassword + " " + (userPassword != null ? userPassword : "");
        config.setPassword(combinedPassword);

        log.info("H2 database encryption enabled (CIPHER=AES)");
    }
}