package com.heronix.controller;

import com.heronix.dto.ReportServiceMesh;
import com.heronix.service.ReportServiceMeshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Service Mesh API Controller
 *
 * REST API endpoints for service mesh management, traffic control,
 * security policies, and microservices observability.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 128 - Report Service Mesh
 */
@RestController
@RequestMapping("/api/service-mesh")
@RequiredArgsConstructor
@Slf4j
public class ReportServiceMeshApiController {

    private final ReportServiceMeshService serviceMeshService;

    /**
     * Create service mesh
     * POST /api/service-mesh
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createServiceMesh(
            @RequestBody ReportServiceMesh serviceMesh) {
        try {
            ReportServiceMesh created = serviceMeshService.createServiceMesh(serviceMesh);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Service mesh created successfully");
            response.put("meshId", created.getMeshId());
            response.put("meshName", created.getMeshName());
            response.put("meshPlatform", created.getMeshPlatform());
            response.put("status", created.getStatus());
            response.put("createdAt", created.getCreatedAt());

            log.info("Service mesh created via API: {}", created.getMeshId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating service mesh: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create service mesh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get service mesh
     * GET /api/service-mesh/{meshId}
     */
    @GetMapping("/{meshId}")
    public ResponseEntity<Map<String, Object>> getServiceMesh(@PathVariable Long meshId) {
        try {
            return serviceMeshService.getServiceMesh(meshId)
                    .map(serviceMesh -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("serviceMesh", serviceMesh);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "Service mesh not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                    });

        } catch (Exception e) {
            log.error("Error retrieving service mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve service mesh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Deploy service mesh
     * POST /api/service-mesh/{meshId}/deploy
     */
    @PostMapping("/{meshId}/deploy")
    public ResponseEntity<Map<String, Object>> deployServiceMesh(@PathVariable Long meshId) {
        try {
            serviceMeshService.deployServiceMesh(meshId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Service mesh deployed successfully");
            response.put("meshId", meshId);
            response.put("deployedAt", LocalDateTime.now());

            log.info("Service mesh deployed via API: {}", meshId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error deploying service mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to deploy service mesh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Register service
     * POST /api/service-mesh/{meshId}/service
     */
    @PostMapping("/{meshId}/service")
    public ResponseEntity<Map<String, Object>> registerService(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> serviceRequest) {
        try {
            String serviceName = (String) serviceRequest.get("serviceName");
            String namespace = (String) serviceRequest.get("namespace");
            String version = (String) serviceRequest.get("version");
            @SuppressWarnings("unchecked")
            List<String> endpoints = (List<String>) serviceRequest.get("endpoints");

            ReportServiceMesh.MeshService meshService = serviceMeshService.registerService(
                    meshId, serviceName, namespace, version, endpoints);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Service registered successfully");
            response.put("serviceId", meshService.getServiceId());
            response.put("serviceName", meshService.getServiceName());
            response.put("namespace", meshService.getNamespace());
            response.put("createdAt", meshService.getCreatedAt());

            log.info("Service registered via API: {} (mesh: {})", meshService.getServiceId(), meshId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error registering service for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to register service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Inject sidecar
     * POST /api/service-mesh/{meshId}/sidecar
     */
    @PostMapping("/{meshId}/sidecar")
    public ResponseEntity<Map<String, Object>> injectSidecar(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> sidecarRequest) {
        try {
            String serviceId = (String) sidecarRequest.get("serviceId");
            String proxyType = (String) sidecarRequest.get("proxyType");
            String proxyVersion = (String) sidecarRequest.get("proxyVersion");
            Integer cpuLimit = (Integer) sidecarRequest.get("cpuLimit");
            Integer memoryLimit = (Integer) sidecarRequest.get("memoryLimit");

            ReportServiceMesh.SidecarProxy sidecar = serviceMeshService.injectSidecar(
                    meshId, serviceId, proxyType, proxyVersion, cpuLimit, memoryLimit);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sidecar injected successfully");
            response.put("sidecarId", sidecar.getSidecarId());
            response.put("serviceId", sidecar.getServiceId());
            response.put("proxyVersion", sidecar.getProxyVersion());
            response.put("status", sidecar.getStatus());
            response.put("startedAt", sidecar.getStartedAt());

            log.info("Sidecar injected via API: {} (mesh: {}, service: {})",
                     sidecar.getSidecarId(), meshId, serviceId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error injecting sidecar for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to inject sidecar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add virtual service
     * POST /api/service-mesh/{meshId}/virtual-service
     */
    @PostMapping("/{meshId}/virtual-service")
    public ResponseEntity<Map<String, Object>> addVirtualService(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> virtualServiceRequest) {
        try {
            String virtualServiceName = (String) virtualServiceRequest.get("virtualServiceName");
            String namespace = (String) virtualServiceRequest.get("namespace");
            @SuppressWarnings("unchecked")
            List<String> hosts = (List<String>) virtualServiceRequest.get("hosts");
            @SuppressWarnings("unchecked")
            List<String> gateways = (List<String>) virtualServiceRequest.get("gateways");
            @SuppressWarnings("unchecked")
            Map<String, Object> routingRules = (Map<String, Object>) virtualServiceRequest.get("routingRules");

            ReportServiceMesh.VirtualService virtualService = serviceMeshService.addVirtualService(
                    meshId, virtualServiceName, namespace, hosts, gateways, routingRules);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Virtual service added successfully");
            response.put("virtualServiceId", virtualService.getVirtualServiceId());
            response.put("virtualServiceName", virtualService.getVirtualServiceName());
            response.put("namespace", virtualService.getNamespace());
            response.put("createdAt", virtualService.getCreatedAt());

            log.info("Virtual service added via API: {} (mesh: {})",
                     virtualService.getVirtualServiceId(), meshId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error adding virtual service for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add virtual service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add destination rule
     * POST /api/service-mesh/{meshId}/destination-rule
     */
    @PostMapping("/{meshId}/destination-rule")
    public ResponseEntity<Map<String, Object>> addDestinationRule(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> ruleRequest) {
        try {
            String ruleName = (String) ruleRequest.get("ruleName");
            String namespace = (String) ruleRequest.get("namespace");
            String host = (String) ruleRequest.get("host");
            String loadBalancer = (String) ruleRequest.get("loadBalancer");
            String trafficPolicy = (String) ruleRequest.get("trafficPolicy");

            ReportServiceMesh.DestinationRule destinationRule = serviceMeshService.addDestinationRule(
                    meshId, ruleName, namespace, host, loadBalancer, trafficPolicy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Destination rule added successfully");
            response.put("ruleId", destinationRule.getRuleId());
            response.put("ruleName", destinationRule.getRuleName());
            response.put("host", destinationRule.getHost());
            response.put("createdAt", destinationRule.getCreatedAt());

            log.info("Destination rule added via API: {} (mesh: {})",
                     destinationRule.getRuleId(), meshId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error adding destination rule for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add destination rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add service entry
     * POST /api/service-mesh/{meshId}/service-entry
     */
    @PostMapping("/{meshId}/service-entry")
    public ResponseEntity<Map<String, Object>> addServiceEntry(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> entryRequest) {
        try {
            String entryName = (String) entryRequest.get("entryName");
            String namespace = (String) entryRequest.get("namespace");
            @SuppressWarnings("unchecked")
            List<String> hosts = (List<String>) entryRequest.get("hosts");
            String location = (String) entryRequest.get("location");
            String resolution = (String) entryRequest.get("resolution");

            ReportServiceMesh.ServiceEntry serviceEntry = serviceMeshService.addServiceEntry(
                    meshId, entryName, namespace, hosts, location, resolution);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Service entry added successfully");
            response.put("entryId", serviceEntry.getEntryId());
            response.put("entryName", serviceEntry.getEntryName());
            response.put("location", serviceEntry.getLocation());
            response.put("createdAt", serviceEntry.getCreatedAt());

            log.info("Service entry added via API: {} (mesh: {})",
                     serviceEntry.getEntryId(), meshId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error adding service entry for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add service entry: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create mesh gateway
     * POST /api/service-mesh/{meshId}/gateway
     */
    @PostMapping("/{meshId}/gateway")
    public ResponseEntity<Map<String, Object>> createMeshGateway(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> gatewayRequest) {
        try {
            String gatewayName = (String) gatewayRequest.get("gatewayName");
            String namespace = (String) gatewayRequest.get("namespace");
            String gatewayType = (String) gatewayRequest.get("gatewayType");
            String selector = (String) gatewayRequest.get("selector");

            ReportServiceMesh.MeshGateway gateway = serviceMeshService.createMeshGateway(
                    meshId, gatewayName, namespace, gatewayType, selector);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mesh gateway created successfully");
            response.put("gatewayId", gateway.getGatewayId());
            response.put("gatewayName", gateway.getGatewayName());
            response.put("namespace", gateway.getNamespace());
            response.put("createdAt", gateway.getCreatedAt());

            log.info("Mesh gateway created via API: {} (mesh: {})",
                     gateway.getGatewayId(), meshId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error creating mesh gateway for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create mesh gateway: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add authorization policy
     * POST /api/service-mesh/{meshId}/authorization-policy
     */
    @PostMapping("/{meshId}/authorization-policy")
    public ResponseEntity<Map<String, Object>> addAuthorizationPolicy(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> policyRequest) {
        try {
            String policyName = (String) policyRequest.get("policyName");
            String namespace = (String) policyRequest.get("namespace");
            String action = (String) policyRequest.get("action");
            @SuppressWarnings("unchecked")
            Map<String, String> selector = (Map<String, String>) policyRequest.get("selector");

            ReportServiceMesh.AuthorizationPolicy policy = serviceMeshService.addAuthorizationPolicy(
                    meshId, policyName, namespace, action, selector);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Authorization policy added successfully");
            response.put("policyId", policy.getPolicyId());
            response.put("policyName", policy.getPolicyName());
            response.put("action", policy.getAction());
            response.put("createdAt", policy.getCreatedAt());

            log.info("Authorization policy added via API: {} (mesh: {})",
                     policy.getPolicyId(), meshId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error adding authorization policy for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add authorization policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Configure peer authentication
     * POST /api/service-mesh/{meshId}/peer-authentication
     */
    @PostMapping("/{meshId}/peer-authentication")
    public ResponseEntity<Map<String, Object>> configurePeerAuthentication(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> authRequest) {
        try {
            String authName = (String) authRequest.get("authName");
            String namespace = (String) authRequest.get("namespace");
            String mtlsMode = (String) authRequest.get("mtlsMode");
            @SuppressWarnings("unchecked")
            Map<String, String> selector = (Map<String, String>) authRequest.get("selector");

            ReportServiceMesh.PeerAuthentication peerAuth = serviceMeshService.configurePeerAuthentication(
                    meshId, authName, namespace, mtlsMode, selector);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Peer authentication configured successfully");
            response.put("peerAuthId", peerAuth.getPeerAuthId());
            response.put("peerAuthName", peerAuth.getPeerAuthName());
            response.put("mtlsMode", peerAuth.getMtlsMode());
            response.put("createdAt", peerAuth.getCreatedAt());

            log.info("Peer authentication configured via API: {} (mesh: {})",
                     peerAuth.getPeerAuthId(), meshId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error configuring peer authentication for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to configure peer authentication: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Configure traffic policy
     * POST /api/service-mesh/{meshId}/traffic-policy
     */
    @PostMapping("/{meshId}/traffic-policy")
    public ResponseEntity<Map<String, Object>> configureTrafficPolicy(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> policyRequest) {
        try {
            String policyName = (String) policyRequest.get("policyName");
            String targetService = (String) policyRequest.get("targetService");
            String loadBalancingStrategy = (String) policyRequest.get("loadBalancingStrategy");
            @SuppressWarnings("unchecked")
            Map<String, Object> retryPolicy = (Map<String, Object>) policyRequest.get("retryPolicy");
            @SuppressWarnings("unchecked")
            Map<String, Object> circuitBreaker = (Map<String, Object>) policyRequest.get("circuitBreaker");

            ReportServiceMesh.TrafficPolicy trafficPolicy = serviceMeshService.configureTrafficPolicy(
                    meshId, policyName, targetService, loadBalancingStrategy, retryPolicy, circuitBreaker);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Traffic policy configured successfully");
            response.put("loadBalancer", trafficPolicy.getLoadBalancer());
            response.put("timestamp", LocalDateTime.now());

            log.info("Traffic policy configured via API: mesh {}", meshId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error configuring traffic policy for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to configure traffic policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Inject fault
     * POST /api/service-mesh/{meshId}/fault-injection
     */
    @PostMapping("/{meshId}/fault-injection")
    public ResponseEntity<Map<String, Object>> injectFault(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> faultRequest) {
        try {
            String faultName = (String) faultRequest.get("faultName");
            String targetService = (String) faultRequest.get("targetService");
            String faultType = (String) faultRequest.get("faultType");
            Double percentage = ((Number) faultRequest.get("percentage")).doubleValue();
            String fixedDelay = (String) faultRequest.get("fixedDelay");
            Integer httpStatus = (Integer) faultRequest.get("httpStatus");

            ReportServiceMesh.FaultInjection fault = serviceMeshService.injectFault(
                    meshId, faultName, targetService, faultType, percentage, fixedDelay, httpStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fault injection configured successfully");
            response.put("faultId", fault.getFaultId());
            response.put("faultName", fault.getFaultName());
            response.put("faultType", fault.getFaultType());
            response.put("percentage", fault.getPercentage());
            response.put("createdAt", fault.getCreatedAt());

            log.info("Fault injection configured via API: {} (mesh: {})",
                     fault.getFaultId(), meshId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error injecting fault for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to inject fault: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Track mesh request
     * POST /api/service-mesh/{meshId}/track-request
     */
    @PostMapping("/{meshId}/track-request")
    public ResponseEntity<Map<String, Object>> trackMeshRequest(
            @PathVariable Long meshId,
            @RequestBody Map<String, Object> requestData) {
        try {
            String sourceService = (String) requestData.get("sourceService");
            String targetService = (String) requestData.get("targetService");
            Boolean success = (Boolean) requestData.get("success");
            Long latency = ((Number) requestData.get("latency")).longValue();

            serviceMeshService.trackMeshRequest(meshId, sourceService, targetService, success, latency);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mesh request tracked successfully");
            response.put("meshId", meshId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error tracking mesh request for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to track mesh request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update sidecar health
     * PUT /api/service-mesh/{meshId}/sidecar/{sidecarId}/health
     */
    @PutMapping("/{meshId}/sidecar/{sidecarId}/health")
    public ResponseEntity<Map<String, Object>> updateSidecarHealth(
            @PathVariable Long meshId,
            @PathVariable String sidecarId,
            @RequestBody Map<String, Object> healthData) {
        try {
            Boolean isHealthy = (Boolean) healthData.get("isHealthy");

            serviceMeshService.updateSidecarHealth(meshId, sidecarId, isHealthy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sidecar health updated successfully");
            response.put("sidecarId", sidecarId);
            response.put("isHealthy", isHealthy);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error updating sidecar health for mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update sidecar health: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete service mesh
     * DELETE /api/service-mesh/{meshId}
     */
    @DeleteMapping("/{meshId}")
    public ResponseEntity<Map<String, Object>> deleteServiceMesh(@PathVariable Long meshId) {
        try {
            serviceMeshService.deleteServiceMesh(meshId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Service mesh deleted successfully");
            response.put("meshId", meshId);

            log.info("Service mesh deleted via API: {}", meshId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting service mesh {}: {}", meshId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete service mesh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get statistics
     * GET /api/service-mesh/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = serviceMeshService.getStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving service mesh statistics: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
