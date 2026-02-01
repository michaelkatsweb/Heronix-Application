package com.heronix.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for communicating with Heronix Talk server REST API.
 * Used by CommunicationCenter to send/receive messages.
 */
@Service
@Slf4j
public class TalkApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${heronix.talk.url:http://localhost:9680}")
    private String talkBaseUrl;

    private String sessionToken;
    private Long userId;
    private String username;

    public TalkApiService(@Qualifier("schedulerRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * Login to Talk server and obtain session token.
     */
    public boolean login(String username, String password) {
        try {
            Map<String, String> body = Map.of("username", username, "password", password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    talkBaseUrl + "/api/auth/login",
                    new HttpEntity<>(body, headers),
                    String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("success") && json.get("success").asBoolean()) {
                    this.sessionToken = json.get("sessionToken").asText();
                    this.userId = json.get("user").get("id").asLong();
                    this.username = username;
                    log.info("Talk login successful: user={}, token={}", username, sessionToken.substring(0, 8) + "...");
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Talk login failed", e);
        }
        return false;
    }

    public boolean isConnected() {
        return sessionToken != null;
    }

    /**
     * Get all channels the user is a member of.
     */
    public List<Map<String, Object>> getChannels() {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    talkBaseUrl + "/api/channels",
                    HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to get channels", e);
        }
        return Collections.emptyList();
    }

    /**
     * Get messages for a channel.
     */
    public List<Map<String, Object>> getChannelMessages(Long channelId, int page, int size) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    talkBaseUrl + "/api/messages/channel/" + channelId + "?page=" + page + "&size=" + size,
                    HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to get messages for channel {}", channelId, e);
        }
        return Collections.emptyList();
    }

    /**
     * Get all messages across all channels the user has access to.
     */
    public List<Map<String, Object>> getAllMessages() {
        List<Map<String, Object>> allMessages = new ArrayList<>();
        List<Map<String, Object>> channels = getChannels();

        for (Map<String, Object> channel : channels) {
            Long channelId = ((Number) channel.get("id")).longValue();
            List<Map<String, Object>> msgs = getChannelMessages(channelId, 0, 50);
            for (Map<String, Object> msg : msgs) {
                msg.put("channelName", channel.get("name"));
            }
            allMessages.addAll(msgs);
        }

        // Sort by timestamp descending
        allMessages.sort((a, b) -> {
            String tsA = (String) a.getOrDefault("timestamp", "");
            String tsB = (String) b.getOrDefault("timestamp", "");
            return tsB.compareTo(tsA);
        });

        return allMessages;
    }

    /**
     * Send a message to a channel.
     */
    public Map<String, Object> sendMessage(Long channelId, String content) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("channelId", channelId);
            body.put("content", content);
            body.put("type", "TEXT");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, authHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(
                    talkBaseUrl + "/api/messages",
                    entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(),
                        new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to send message to channel {}", channelId, e);
        }
        return null;
    }

    /**
     * Search messages.
     */
    public List<Map<String, Object>> searchMessages(String query) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    talkBaseUrl + "/api/messages/search?q=" + query + "&page=0&size=50",
                    HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to search messages", e);
        }
        return Collections.emptyList();
    }

    /**
     * Get active news items.
     */
    public List<Map<String, Object>> getNewsItems() {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    talkBaseUrl + "/api/news",
                    HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            log.debug("Failed to get news items (endpoint may not exist)");
        }
        return Collections.emptyList();
    }

    /**
     * Get active emergency alerts.
     */
    public List<Map<String, Object>> getAlerts() {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    talkBaseUrl + "/api/alerts/active",
                    HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            log.debug("Failed to get alerts (endpoint may not exist)");
        }
        return Collections.emptyList();
    }

    /**
     * Get all users (for recipient selection).
     */
    public List<Map<String, Object>> getUsers() {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    talkBaseUrl + "/api/users",
                    HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            log.debug("Failed to get users");
        }
        return Collections.emptyList();
    }

    /**
     * Create or get a DM channel with another user.
     */
    public Map<String, Object> getOrCreateDm(Long otherUserId) {
        try {
            Map<String, Object> body = Map.of("targetUserId", otherUserId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, authHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(
                    talkBaseUrl + "/api/channels/dm",
                    entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(),
                        new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to create DM channel", e);
        }
        return null;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (sessionToken != null) {
            headers.set("X-Session-Token", sessionToken);
        }
        return headers;
    }
}
