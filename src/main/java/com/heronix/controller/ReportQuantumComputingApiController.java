package com.heronix.controller;

import com.heronix.dto.ReportQuantumComputing;
import com.heronix.service.ReportQuantumComputingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Quantum Computing & Optimization API Controller
 *
 * REST API endpoints for quantum computing operations and optimization.
 *
 * Endpoints:
 * - POST /api/quantum-computing - Create quantum system
 * - GET /api/quantum-computing/{id} - Get system
 * - POST /api/quantum-computing/{id}/initialize - Initialize system
 * - POST /api/quantum-computing/{id}/circuit - Create circuit
 * - POST /api/quantum-computing/{id}/job - Submit job
 * - POST /api/quantum-computing/{id}/job/execute - Execute job
 * - POST /api/quantum-computing/{id}/job/complete - Complete job
 * - POST /api/quantum-computing/{id}/algorithm - Register algorithm
 * - POST /api/quantum-computing/{id}/problem - Add optimization problem
 * - POST /api/quantum-computing/{id}/problem/solve - Solve problem
 * - POST /api/quantum-computing/{id}/qubit - Allocate qubit
 * - POST /api/quantum-computing/{id}/measurement - Record measurement
 * - POST /api/quantum-computing/{id}/error-correction - Configure error correction
 * - POST /api/quantum-computing/{id}/simulation - Run simulation
 * - POST /api/quantum-computing/{id}/result - Store result
 * - DELETE /api/quantum-computing/{id} - Delete system
 * - GET /api/quantum-computing/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 112 - Report Quantum Computing & Optimization
 */
@RestController
@RequestMapping("/api/quantum-computing")
@RequiredArgsConstructor
@Slf4j
public class ReportQuantumComputingApiController {

    private final ReportQuantumComputingService quantumService;

    /**
     * Create quantum system
     */
    @PostMapping
    public ResponseEntity<ReportQuantumComputing> createSystem(@RequestBody ReportQuantumComputing system) {
        log.info("POST /api/quantum-computing - Creating quantum system: {}", system.getSystemName());

        try {
            ReportQuantumComputing created = quantumService.createSystem(system);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating quantum system", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get quantum system
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportQuantumComputing> getSystem(@PathVariable Long id) {
        log.info("GET /api/quantum-computing/{}", id);

        try {
            return quantumService.getSystem(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching quantum system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Initialize system
     */
    @PostMapping("/{id}/initialize")
    public ResponseEntity<Map<String, Object>> initializeSystem(@PathVariable Long id) {
        log.info("POST /api/quantum-computing/{}/initialize", id);

        try {
            quantumService.initializeSystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quantum system initialized");
            response.put("systemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error initializing quantum system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create circuit
     */
    @PostMapping("/{id}/circuit")
    public ResponseEntity<ReportQuantumComputing.QuantumCircuit> createCircuit(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/circuit", id);

        try {
            String circuitName = (String) request.get("circuitName");
            Integer width = request.get("width") != null ?
                    ((Number) request.get("width")).intValue() : 4;
            @SuppressWarnings("unchecked")
            List<ReportQuantumComputing.QuantumGate> gates =
                    (List<ReportQuantumComputing.QuantumGate>) request.get("gates");

            ReportQuantumComputing.QuantumCircuit circuit = quantumService.createCircuit(
                    id, circuitName, width, gates
            );

            return ResponseEntity.ok(circuit);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating circuit: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Submit job
     */
    @PostMapping("/{id}/job")
    public ResponseEntity<ReportQuantumComputing.QuantumJob> submitJob(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/job", id);

        try {
            String jobName = (String) request.get("jobName");
            String circuitId = (String) request.get("circuitId");
            Integer shots = request.get("shots") != null ?
                    ((Number) request.get("shots")).intValue() : 1024;
            String backend = (String) request.get("backend");

            ReportQuantumComputing.QuantumJob job = quantumService.submitJob(
                    id, jobName, circuitId, shots, backend
            );

            return ResponseEntity.ok(job);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error submitting job: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Execute job
     */
    @PostMapping("/{id}/job/execute")
    public ResponseEntity<Map<String, Object>> executeJob(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/quantum-computing/{}/job/execute", id);

        try {
            String jobId = request.get("jobId");

            quantumService.executeJob(id, jobId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quantum job started");
            response.put("jobId", jobId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system or job not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing job: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete job
     */
    @PostMapping("/{id}/job/complete")
    public ResponseEntity<Map<String, Object>> completeJob(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/job/complete", id);

        try {
            String jobId = (String) request.get("jobId");
            Boolean success = (Boolean) request.getOrDefault("success", true);

            quantumService.completeJob(id, jobId, success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quantum job completed");
            response.put("jobId", jobId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system or job not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing job: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register algorithm
     */
    @PostMapping("/{id}/algorithm")
    public ResponseEntity<ReportQuantumComputing.QuantumAlgorithm> registerAlgorithm(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/algorithm", id);

        try {
            String algorithmName = (String) request.get("algorithmName");
            String algorithmTypeStr = (String) request.get("algorithmType");
            Integer requiredQubits = request.get("requiredQubits") != null ?
                    ((Number) request.get("requiredQubits")).intValue() : 4;

            ReportQuantumComputing.AlgorithmType algorithmType =
                    ReportQuantumComputing.AlgorithmType.valueOf(algorithmTypeStr);

            ReportQuantumComputing.QuantumAlgorithm algorithm = quantumService.registerAlgorithm(
                    id, algorithmName, algorithmType, requiredQubits
            );

            return ResponseEntity.ok(algorithm);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering algorithm: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add optimization problem
     */
    @PostMapping("/{id}/problem")
    public ResponseEntity<ReportQuantumComputing.OptimizationProblem> addProblem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/problem", id);

        try {
            String problemName = (String) request.get("problemName");
            String problemTypeStr = (String) request.get("problemType");
            Integer variableCount = request.get("variableCount") != null ?
                    ((Number) request.get("variableCount")).intValue() : 10;
            String objectiveFunction = (String) request.get("objectiveFunction");

            ReportQuantumComputing.OptimizationType problemType =
                    ReportQuantumComputing.OptimizationType.valueOf(problemTypeStr);

            ReportQuantumComputing.OptimizationProblem problem = quantumService.addOptimizationProblem(
                    id, problemName, problemType, variableCount, objectiveFunction
            );

            return ResponseEntity.ok(problem);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding optimization problem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Solve problem
     */
    @PostMapping("/{id}/problem/solve")
    public ResponseEntity<Map<String, Object>> solveProblem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/problem/solve", id);

        try {
            String problemId = (String) request.get("problemId");
            @SuppressWarnings("unchecked")
            Map<String, Object> solution = (Map<String, Object>) request.get("solution");
            Double optimalValue = request.get("optimalValue") != null ?
                    ((Number) request.get("optimalValue")).doubleValue() : 0.0;

            quantumService.solveProblem(id, problemId, solution, optimalValue);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Optimization problem solved");
            response.put("problemId", problemId);
            response.put("optimalValue", optimalValue);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system or problem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error solving problem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Allocate qubit
     */
    @PostMapping("/{id}/qubit")
    public ResponseEntity<ReportQuantumComputing.Qubit> allocateQubit(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/qubit", id);

        try {
            Integer qubitIndex = request.get("qubitIndex") != null ?
                    ((Number) request.get("qubitIndex")).intValue() : 0;

            ReportQuantumComputing.Qubit qubit = quantumService.allocateQubit(id, qubitIndex);

            return ResponseEntity.ok(qubit);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error allocating qubit: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record measurement
     */
    @PostMapping("/{id}/measurement")
    public ResponseEntity<ReportQuantumComputing.Measurement> recordMeasurement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/measurement", id);

        try {
            String jobId = (String) request.get("jobId");
            @SuppressWarnings("unchecked")
            List<Integer> measuredQubits = (List<Integer>) request.get("measuredQubits");
            String bitString = (String) request.get("bitString");
            Integer counts = request.get("counts") != null ?
                    ((Number) request.get("counts")).intValue() : 1;

            ReportQuantumComputing.Measurement measurement = quantumService.recordMeasurement(
                    id, jobId, measuredQubits, bitString, counts
            );

            return ResponseEntity.ok(measurement);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording measurement: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Configure error correction
     */
    @PostMapping("/{id}/error-correction")
    public ResponseEntity<Map<String, Object>> configureErrorCorrection(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/error-correction", id);

        try {
            String schemeName = (String) request.get("schemeName");
            String schemeType = (String) request.get("schemeType");
            Integer logicalQubits = request.get("logicalQubits") != null ?
                    ((Number) request.get("logicalQubits")).intValue() : 1;
            Integer physicalQubits = request.get("physicalQubits") != null ?
                    ((Number) request.get("physicalQubits")).intValue() : 9;

            quantumService.configureErrorCorrection(id, schemeName, schemeType, logicalQubits, physicalQubits);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error correction configured");
            response.put("schemeName", schemeName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error configuring error correction: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Run simulation
     */
    @PostMapping("/{id}/simulation")
    public ResponseEntity<ReportQuantumComputing.QuantumSimulation> runSimulation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/simulation", id);

        try {
            String simulationName = (String) request.get("simulationName");
            String simulationType = (String) request.get("simulationType");
            Integer qubitCount = request.get("qubitCount") != null ?
                    ((Number) request.get("qubitCount")).intValue() : 4;

            ReportQuantumComputing.QuantumSimulation simulation = quantumService.runSimulation(
                    id, simulationName, simulationType, qubitCount
            );

            return ResponseEntity.ok(simulation);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error running simulation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Store result
     */
    @PostMapping("/{id}/result")
    public ResponseEntity<ReportQuantumComputing.QuantumResult> storeResult(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum-computing/{}/result", id);

        try {
            String jobId = (String) request.get("jobId");
            Integer totalShots = request.get("totalShots") != null ?
                    ((Number) request.get("totalShots")).intValue() : 1024;
            @SuppressWarnings("unchecked")
            Map<String, Integer> counts = (Map<String, Integer>) request.get("counts");

            ReportQuantumComputing.QuantumResult result = quantumService.storeResult(
                    id, jobId, totalShots, counts
            );

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error storing result: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete system
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSystem(@PathVariable Long id) {
        log.info("DELETE /api/quantum-computing/{}", id);

        try {
            quantumService.deleteSystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quantum system deleted");
            response.put("systemId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting quantum system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/quantum-computing/stats");

        try {
            Map<String, Object> stats = quantumService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching quantum computing statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
