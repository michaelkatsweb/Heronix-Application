package com.heronix.service;

import com.heronix.model.domain.Substitute;
import com.heronix.model.enums.SubstituteSource;
import com.heronix.model.enums.SubstituteType;
import com.heronix.repository.SubstituteRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service for importing substitute teachers from CSV files
 * Supports third-party substitute management services like Kelly Services, ESS, Source4Teachers, etc.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Substitute Import
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubstituteImportService {

    private final SubstituteRepository substituteRepository;

    // Common date formats used by third-party systems
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    );

    /**
     * Result of a CSV import operation
     */
    public static class ImportResult {
        private int totalRows = 0;
        private int imported = 0;
        private int updated = 0;
        private int skipped = 0;
        private int errors = 0;
        private List<String> errorMessages = new ArrayList<>();
        private String importReference;

        public int getTotalRows() { return totalRows; }
        public int getImported() { return imported; }
        public int getUpdated() { return updated; }
        public int getSkipped() { return skipped; }
        public int getErrors() { return errors; }
        public List<String> getErrorMessages() { return errorMessages; }
        public String getImportReference() { return importReference; }

        public String getSummary() {
            return String.format("Import complete: %d total rows, %d new, %d updated, %d skipped, %d errors",
                    totalRows, imported, updated, skipped, errors);
        }
    }

    /**
     * Configuration for CSV import mapping
     */
    public static class ImportConfig {
        private String agencyName;
        private int firstNameCol = -1;
        private int lastNameCol = -1;
        private int employeeIdCol = -1;
        private int emailCol = -1;
        private int phoneCol = -1;
        private int typeCol = -1;
        private int certificationsCol = -1;
        private int validFromCol = -1;
        private int validUntilCol = -1;
        private int hourlyRateCol = -1;
        private int dailyRateCol = -1;
        private int notesCol = -1;
        private boolean hasHeaderRow = true;
        private boolean updateExisting = true;

        // Builder-style setters
        public ImportConfig agencyName(String val) { this.agencyName = val; return this; }
        public ImportConfig firstNameCol(int val) { this.firstNameCol = val; return this; }
        public ImportConfig lastNameCol(int val) { this.lastNameCol = val; return this; }
        public ImportConfig employeeIdCol(int val) { this.employeeIdCol = val; return this; }
        public ImportConfig emailCol(int val) { this.emailCol = val; return this; }
        public ImportConfig phoneCol(int val) { this.phoneCol = val; return this; }
        public ImportConfig typeCol(int val) { this.typeCol = val; return this; }
        public ImportConfig certificationsCol(int val) { this.certificationsCol = val; return this; }
        public ImportConfig validFromCol(int val) { this.validFromCol = val; return this; }
        public ImportConfig validUntilCol(int val) { this.validUntilCol = val; return this; }
        public ImportConfig hourlyRateCol(int val) { this.hourlyRateCol = val; return this; }
        public ImportConfig dailyRateCol(int val) { this.dailyRateCol = val; return this; }
        public ImportConfig notesCol(int val) { this.notesCol = val; return this; }
        public ImportConfig hasHeaderRow(boolean val) { this.hasHeaderRow = val; return this; }
        public ImportConfig updateExisting(boolean val) { this.updateExisting = val; return this; }

        // Getters
        public String getAgencyName() { return agencyName; }
        public int getFirstNameCol() { return firstNameCol; }
        public int getLastNameCol() { return lastNameCol; }
        public int getEmployeeIdCol() { return employeeIdCol; }
        public int getEmailCol() { return emailCol; }
        public int getPhoneCol() { return phoneCol; }
        public int getTypeCol() { return typeCol; }
        public int getCertificationsCol() { return certificationsCol; }
        public int getValidFromCol() { return validFromCol; }
        public int getValidUntilCol() { return validUntilCol; }
        public int getHourlyRateCol() { return hourlyRateCol; }
        public int getDailyRateCol() { return dailyRateCol; }
        public int getNotesCol() { return notesCol; }
        public boolean isHasHeaderRow() { return hasHeaderRow; }
        public boolean isUpdateExisting() { return updateExisting; }
    }

    /**
     * Import substitutes from a CSV file with custom column mapping
     */
    @Transactional
    public ImportResult importFromCsv(File csvFile, ImportConfig config) throws IOException {
        log.info("Starting CSV import from file: {} for agency: {}", csvFile.getName(), config.getAgencyName());

        ImportResult result = new ImportResult();
        result.importReference = generateImportReference(config.getAgencyName());

        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            processCSV(reader, config, result);
        } catch (CsvValidationException e) {
            log.error("CSV validation error", e);
            result.errors++;
            result.errorMessages.add("CSV format error: " + e.getMessage());
        }

        log.info("Import complete: {}", result.getSummary());
        return result;
    }

    /**
     * Import substitutes from a CSV input stream
     */
    @Transactional
    public ImportResult importFromCsv(InputStream inputStream, ImportConfig config) throws IOException {
        log.info("Starting CSV import from stream for agency: {}", config.getAgencyName());

        ImportResult result = new ImportResult();
        result.importReference = generateImportReference(config.getAgencyName());

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            processCSV(reader, config, result);
        } catch (CsvValidationException e) {
            log.error("CSV validation error", e);
            result.errors++;
            result.errorMessages.add("CSV format error: " + e.getMessage());
        }

        log.info("Import complete: {}", result.getSummary());
        return result;
    }

    /**
     * Auto-detect column mapping from CSV header row
     */
    public ImportConfig autoDetectColumns(String[] headerRow, String agencyName) {
        ImportConfig config = new ImportConfig();
        config.agencyName(agencyName);
        config.hasHeaderRow(true);

        for (int i = 0; i < headerRow.length; i++) {
            String header = headerRow[i].toLowerCase().trim();

            // First name detection
            if (matchesAny(header, "first name", "firstname", "first_name", "fname", "given name")) {
                config.firstNameCol(i);
            }
            // Last name detection
            else if (matchesAny(header, "last name", "lastname", "last_name", "lname", "surname", "family name")) {
                config.lastNameCol(i);
            }
            // Employee ID detection
            else if (matchesAny(header, "employee id", "employeeid", "employee_id", "emp id", "id", "sub id", "substitute id")) {
                config.employeeIdCol(i);
            }
            // Email detection
            else if (matchesAny(header, "email", "e-mail", "email address", "email_address")) {
                config.emailCol(i);
            }
            // Phone detection
            else if (matchesAny(header, "phone", "phone number", "phonenumber", "phone_number", "telephone", "cell", "mobile")) {
                config.phoneCol(i);
            }
            // Type detection
            else if (matchesAny(header, "type", "sub type", "substitute type", "certification type", "position")) {
                config.typeCol(i);
            }
            // Certifications detection
            else if (matchesAny(header, "certifications", "certification", "subjects", "qualified subjects", "credentials")) {
                config.certificationsCol(i);
            }
            // Valid from detection
            else if (matchesAny(header, "valid from", "validfrom", "start date", "effective date", "available from")) {
                config.validFromCol(i);
            }
            // Valid until detection
            else if (matchesAny(header, "valid until", "validuntil", "end date", "expiration", "expires", "available until")) {
                config.validUntilCol(i);
            }
            // Hourly rate detection
            else if (matchesAny(header, "hourly rate", "hourlyrate", "hourly_rate", "rate/hour", "hourly")) {
                config.hourlyRateCol(i);
            }
            // Daily rate detection
            else if (matchesAny(header, "daily rate", "dailyrate", "daily_rate", "rate/day", "daily")) {
                config.dailyRateCol(i);
            }
            // Notes detection
            else if (matchesAny(header, "notes", "note", "comments", "remarks", "additional info")) {
                config.notesCol(i);
            }
        }

        log.info("Auto-detected columns for agency '{}': firstName={}, lastName={}, empId={}, email={}, type={}",
                agencyName, config.firstNameCol, config.lastNameCol, config.employeeIdCol,
                config.emailCol, config.typeCol);

        return config;
    }

    /**
     * Read header row from CSV file for column detection
     */
    public String[] readHeaderRow(File csvFile) throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            return reader.readNext();
        } catch (CsvValidationException e) {
            throw new IOException("Failed to read CSV header", e);
        }
    }

    /**
     * Preview first N rows of CSV file
     */
    public List<String[]> previewCsv(File csvFile, int maxRows) throws IOException {
        List<String[]> preview = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            String[] row;
            int count = 0;
            while ((row = reader.readNext()) != null && count < maxRows) {
                preview.add(row);
                count++;
            }
        } catch (CsvValidationException e) {
            throw new IOException("Failed to preview CSV", e);
        }
        return preview;
    }

    /**
     * Get list of known third-party agencies with preset configurations
     */
    public Map<String, ImportConfig> getKnownAgencyConfigs() {
        Map<String, ImportConfig> configs = new LinkedHashMap<>();

        // Kelly Services typical format
        configs.put("Kelly Services", new ImportConfig()
                .agencyName("Kelly Services")
                .firstNameCol(0)
                .lastNameCol(1)
                .employeeIdCol(2)
                .emailCol(3)
                .phoneCol(4)
                .typeCol(5)
                .certificationsCol(6)
                .validFromCol(7)
                .validUntilCol(8));

        // ESS (formerly Source4Teachers)
        configs.put("ESS", new ImportConfig()
                .agencyName("ESS")
                .employeeIdCol(0)
                .lastNameCol(1)
                .firstNameCol(2)
                .emailCol(3)
                .phoneCol(4)
                .certificationsCol(5)
                .typeCol(6));

        // Swing Education
        configs.put("Swing Education", new ImportConfig()
                .agencyName("Swing Education")
                .firstNameCol(0)
                .lastNameCol(1)
                .emailCol(2)
                .phoneCol(3)
                .typeCol(4)
                .certificationsCol(5)
                .dailyRateCol(6));

        // Generic/Custom
        configs.put("Custom", new ImportConfig()
                .agencyName("Custom Agency"));

        return configs;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private void processCSV(CSVReader reader, ImportConfig config, ImportResult result)
            throws IOException, CsvValidationException {

        String[] row;
        int rowNum = 0;

        // Skip header if present
        if (config.isHasHeaderRow()) {
            reader.readNext();
            rowNum++;
        }

        while ((row = reader.readNext()) != null) {
            rowNum++;
            result.totalRows++;

            try {
                processRow(row, rowNum, config, result);
            } catch (Exception e) {
                result.errors++;
                result.errorMessages.add(String.format("Row %d: %s", rowNum, e.getMessage()));
                log.warn("Error processing row {}: {}", rowNum, e.getMessage());
            }
        }
    }

    private void processRow(String[] row, int rowNum, ImportConfig config, ImportResult result) {
        // Validate required fields
        String firstName = getColumnValue(row, config.getFirstNameCol());
        String lastName = getColumnValue(row, config.getLastNameCol());

        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            result.skipped++;
            result.errorMessages.add(String.format("Row %d: Missing first or last name", rowNum));
            return;
        }

        // Check if substitute already exists (by employee ID or email)
        String employeeId = getColumnValue(row, config.getEmployeeIdCol());
        String email = getColumnValue(row, config.getEmailCol());

        Substitute existing = null;
        if (employeeId != null && !employeeId.isEmpty()) {
            existing = substituteRepository.findByEmployeeId(employeeId).orElse(null);
        }
        if (existing == null && email != null && !email.isEmpty()) {
            existing = substituteRepository.findByEmail(email).orElse(null);
        }

        if (existing != null && !config.isUpdateExisting()) {
            result.skipped++;
            return;
        }

        Substitute substitute = existing != null ? existing : new Substitute();
        boolean isNew = existing == null;

        // Populate fields
        substitute.setFirstName(firstName);
        substitute.setLastName(lastName);
        substitute.setSource(SubstituteSource.THIRD_PARTY);
        substitute.setAgencyName(config.getAgencyName());
        substitute.setImportReference(result.importReference);

        if (employeeId != null && !employeeId.isEmpty()) {
            substitute.setEmployeeId(employeeId);
        }

        if (email != null && !email.isEmpty()) {
            substitute.setEmail(email);
        }

        String phone = getColumnValue(row, config.getPhoneCol());
        if (phone != null && !phone.isEmpty()) {
            substitute.setPhoneNumber(phone);
        }

        // Parse type
        String typeStr = getColumnValue(row, config.getTypeCol());
        substitute.setType(parseSubstituteType(typeStr));

        // Parse certifications
        String certs = getColumnValue(row, config.getCertificationsCol());
        if (certs != null && !certs.isEmpty()) {
            Set<String> certSet = new HashSet<>();
            for (String cert : certs.split("[,;|]")) {
                String trimmed = cert.trim();
                if (!trimmed.isEmpty()) {
                    certSet.add(trimmed);
                }
            }
            substitute.setCertifications(certSet);
        }

        // Parse dates
        LocalDate validFrom = parseDate(getColumnValue(row, config.getValidFromCol()));
        LocalDate validUntil = parseDate(getColumnValue(row, config.getValidUntilCol()));

        if (validFrom != null) {
            substitute.setValidFrom(validFrom);
            substitute.setTemporary(true);
        }
        if (validUntil != null) {
            substitute.setValidUntil(validUntil);
            substitute.setTemporary(true);
        }

        // Parse rates
        Double hourlyRate = parseDouble(getColumnValue(row, config.getHourlyRateCol()));
        Double dailyRate = parseDouble(getColumnValue(row, config.getDailyRateCol()));

        if (hourlyRate != null) {
            substitute.setHourlyRate(hourlyRate);
        }
        if (dailyRate != null) {
            substitute.setDailyRate(dailyRate);
        }

        // Notes
        String notes = getColumnValue(row, config.getNotesCol());
        if (notes != null && !notes.isEmpty()) {
            substitute.setNotes(notes);
        }

        // Save
        substituteRepository.save(substitute);

        if (isNew) {
            result.imported++;
            log.debug("Imported new substitute: {} {} ({})", firstName, lastName, employeeId);
        } else {
            result.updated++;
            log.debug("Updated existing substitute: {} {} ({})", firstName, lastName, employeeId);
        }
    }

    private String getColumnValue(String[] row, int colIndex) {
        if (colIndex < 0 || colIndex >= row.length) {
            return null;
        }
        String value = row[colIndex];
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }

    private SubstituteType parseSubstituteType(String typeStr) {
        if (typeStr == null || typeStr.isEmpty()) {
            return SubstituteType.CERTIFIED_TEACHER; // Default
        }

        String lower = typeStr.toLowerCase().trim();

        if (lower.contains("certified") && !lower.contains("uncertified") && !lower.contains("non-certified")) {
            return SubstituteType.CERTIFIED_TEACHER;
        }
        if (lower.contains("uncertified") || lower.contains("non-certified")) {
            return SubstituteType.UNCERTIFIED_SUBSTITUTE;
        }
        if (lower.contains("para") || lower.contains("aide") || lower.contains("assistant")) {
            return SubstituteType.PARAPROFESSIONAL;
        }
        if (lower.contains("long") && lower.contains("term")) {
            return SubstituteType.LONG_TERM_SUBSTITUTE;
        }
        // Default for teacher, licensed, emergency, or unknown types
        if (lower.contains("teacher") || lower.contains("licensed")) {
            return SubstituteType.CERTIFIED_TEACHER;
        }

        return SubstituteType.UNCERTIFIED_SUBSTITUTE;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        log.warn("Could not parse date: {}", dateStr);
        return null;
    }

    private Double parseDouble(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        try {
            // Remove currency symbols and commas
            String cleaned = str.replaceAll("[^\\d.]", "");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean matchesAny(String value, String... patterns) {
        for (String pattern : patterns) {
            if (value.equals(pattern) || value.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String generateImportReference(String agencyName) {
        String prefix = agencyName != null ? agencyName.substring(0, Math.min(3, agencyName.length())).toUpperCase() : "IMP";
        return prefix + "-" + System.currentTimeMillis();
    }

    /**
     * Get substitutes by import reference (for reviewing a specific import batch)
     */
    @Transactional(readOnly = true)
    public List<Substitute> getSubstitutesByImportReference(String importReference) {
        return substituteRepository.findByImportReference(importReference);
    }

    /**
     * Rollback an import by deleting all substitutes with the given import reference
     */
    @Transactional
    public int rollbackImport(String importReference) {
        List<Substitute> toDelete = substituteRepository.findByImportReference(importReference);
        int count = toDelete.size();
        substituteRepository.deleteAll(toDelete);
        log.info("Rolled back import {}: deleted {} substitutes", importReference, count);
        return count;
    }

    /**
     * Deactivate all third-party substitutes from a specific agency
     * Useful when ending a contract with an agency
     */
    @Transactional
    public int deactivateAgencySubstitutes(String agencyName) {
        List<Substitute> subs = substituteRepository.findByAgencyName(agencyName);
        int count = 0;
        for (Substitute sub : subs) {
            if (sub.getActive()) {
                sub.setActive(false);
                substituteRepository.save(sub);
                count++;
            }
        }
        log.info("Deactivated {} substitutes from agency: {}", count, agencyName);
        return count;
    }
}
