package com.heronix.ui.validation;

import javafx.animation.PauseTransition;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Form Validator
 * A comprehensive form validation framework for JavaFX.
 *
 * Features:
 * - Real-time validation as user types
 * - Visual error indicators
 * - Error message display
 * - Multiple validators per field
 * - Required field handling
 * - Custom validation rules
 * - Validation summary
 * - Form-level validation
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class FormValidator {

    // ========================================================================
    // PSEUDO CLASSES FOR CSS STYLING
    // ========================================================================

    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");
    private static final PseudoClass VALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("valid");

    // ========================================================================
    // FIELD REGISTRATIONS
    // ========================================================================

    private final Map<Node, FieldValidator<?>> fieldValidators = new LinkedHashMap<>();
    private final Map<Node, Label> errorLabels = new HashMap<>();

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    private final BooleanProperty valid = new SimpleBooleanProperty(true);
    private final ObservableList<ValidationError> errors = FXCollections.observableArrayList();

    // ========================================================================
    // CONFIGURATION
    // ========================================================================

    private boolean validateOnChange = true;
    private boolean showErrorLabels = true;
    private Duration debounceDelay = Duration.millis(300);

    // ========================================================================
    // PUBLIC API - FIELD REGISTRATION
    // ========================================================================

    /**
     * Register a TextField for validation
     */
    public FieldValidator<String> registerTextField(TextField field, String fieldName) {
        FieldValidator<String> validator = new FieldValidator<>(fieldName);

        // Value extractor
        validator.setValueExtractor(() -> field.getText());

        // Real-time validation with debounce
        if (validateOnChange) {
            PauseTransition debounce = new PauseTransition(debounceDelay);
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                debounce.setOnFinished(e -> validateField(field, validator));
                debounce.playFromStart();
            });
        }

        // Focus lost validation
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateField(field, validator);
            }
        });

        fieldValidators.put(field, validator);
        return validator;
    }

    /**
     * Register a PasswordField for validation
     */
    public FieldValidator<String> registerPasswordField(PasswordField field, String fieldName) {
        FieldValidator<String> validator = new FieldValidator<>(fieldName);
        validator.setValueExtractor(() -> field.getText());

        if (validateOnChange) {
            PauseTransition debounce = new PauseTransition(debounceDelay);
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                debounce.setOnFinished(e -> validateField(field, validator));
                debounce.playFromStart();
            });
        }

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateField(field, validator);
            }
        });

        fieldValidators.put(field, validator);
        return validator;
    }

    /**
     * Register a ComboBox for validation
     */
    public <T> FieldValidator<T> registerComboBox(ComboBox<T> field, String fieldName) {
        FieldValidator<T> validator = new FieldValidator<>(fieldName);
        validator.setValueExtractor(() -> field.getValue());

        field.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateField(field, validator);
        });

        fieldValidators.put(field, validator);
        return validator;
    }

    /**
     * Register a DatePicker for validation
     */
    public FieldValidator<java.time.LocalDate> registerDatePicker(DatePicker field, String fieldName) {
        FieldValidator<java.time.LocalDate> validator = new FieldValidator<>(fieldName);
        validator.setValueExtractor(() -> field.getValue());

        field.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateField(field, validator);
        });

        fieldValidators.put(field, validator);
        return validator;
    }

    /**
     * Register a CheckBox for validation
     */
    public FieldValidator<Boolean> registerCheckBox(CheckBox field, String fieldName) {
        FieldValidator<Boolean> validator = new FieldValidator<>(fieldName);
        validator.setValueExtractor(() -> field.isSelected());

        field.selectedProperty().addListener((obs, oldVal, newVal) -> {
            validateField(field, validator);
        });

        fieldValidators.put(field, validator);
        return validator;
    }

    /**
     * Register a TextArea for validation
     */
    public FieldValidator<String> registerTextArea(TextArea field, String fieldName) {
        FieldValidator<String> validator = new FieldValidator<>(fieldName);
        validator.setValueExtractor(() -> field.getText());

        if (validateOnChange) {
            PauseTransition debounce = new PauseTransition(debounceDelay);
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                debounce.setOnFinished(e -> validateField(field, validator));
                debounce.playFromStart();
            });
        }

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateField(field, validator);
            }
        });

        fieldValidators.put(field, validator);
        return validator;
    }

    /**
     * Register a Spinner for validation
     */
    public <T> FieldValidator<T> registerSpinner(Spinner<T> field, String fieldName) {
        FieldValidator<T> validator = new FieldValidator<>(fieldName);
        validator.setValueExtractor(() -> field.getValue());

        field.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateField(field, validator);
        });

        fieldValidators.put(field, validator);
        return validator;
    }

    // ========================================================================
    // PUBLIC API - VALIDATION
    // ========================================================================

    /**
     * Validate all registered fields
     */
    public boolean validateAll() {
        errors.clear();
        boolean allValid = true;

        for (Map.Entry<Node, FieldValidator<?>> entry : fieldValidators.entrySet()) {
            Node field = entry.getKey();
            FieldValidator<?> validator = entry.getValue();

            if (!validateField(field, validator)) {
                allValid = false;
            }
        }

        valid.set(allValid);
        return allValid;
    }

    /**
     * Validate a specific field
     */
    @SuppressWarnings("unchecked")
    private <T> boolean validateField(Node field, FieldValidator<T> validator) {
        List<String> fieldErrors = validator.validate();

        // Remove old errors for this field
        errors.removeIf(e -> e.getFieldName().equals(validator.getFieldName()));

        if (fieldErrors.isEmpty()) {
            // Valid
            setFieldValid(field, true);
            hideErrorLabel(field);
            return true;
        } else {
            // Invalid
            setFieldValid(field, false);
            showErrorLabel(field, fieldErrors.get(0));

            // Add to error list
            for (String error : fieldErrors) {
                errors.add(new ValidationError(validator.getFieldName(), error));
            }

            updateValidProperty();
            return false;
        }
    }

    /**
     * Clear all validation states
     */
    public void clearValidation() {
        errors.clear();
        valid.set(true);

        for (Node field : fieldValidators.keySet()) {
            clearFieldValidation(field);
        }
    }

    /**
     * Clear validation for a specific field
     */
    public void clearFieldValidation(Node field) {
        field.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, false);
        field.pseudoClassStateChanged(VALID_PSEUDO_CLASS, false);
        hideErrorLabel(field);
    }

    /**
     * Check if form is currently valid
     */
    public boolean isValid() {
        return valid.get();
    }

    /**
     * Get all current errors
     */
    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Get error summary as string
     */
    public String getErrorSummary() {
        if (errors.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (ValidationError error : errors) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("• ").append(error.getFieldName()).append(": ").append(error.getMessage());
        }
        return sb.toString();
    }

    // ========================================================================
    // PUBLIC API - CONFIGURATION
    // ========================================================================

    /**
     * Set whether to validate on change
     */
    public void setValidateOnChange(boolean validateOnChange) {
        this.validateOnChange = validateOnChange;
    }

    /**
     * Set whether to show error labels
     */
    public void setShowErrorLabels(boolean showErrorLabels) {
        this.showErrorLabels = showErrorLabels;
    }

    /**
     * Set debounce delay for real-time validation
     */
    public void setDebounceDelay(Duration delay) {
        this.debounceDelay = delay;
    }

    /**
     * Get valid property for binding
     */
    public BooleanProperty validProperty() {
        return valid;
    }

    /**
     * Get errors list for binding
     */
    public ObservableList<ValidationError> errorsProperty() {
        return errors;
    }

    // ========================================================================
    // INTERNAL - UI UPDATES
    // ========================================================================

    private void setFieldValid(Node field, boolean isValid) {
        field.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, !isValid);
        field.pseudoClassStateChanged(VALID_PSEUDO_CLASS, isValid);

        // Apply inline style for immediate visual feedback
        if (field instanceof Control) {
            Control control = (Control) field;
            if (!isValid) {
                control.setStyle(control.getStyle() + "; -fx-border-color: #EF4444;");
            } else {
                control.setStyle(control.getStyle().replace("; -fx-border-color: #EF4444;", ""));
            }
        }
    }

    private void showErrorLabel(Node field, String message) {
        if (!showErrorLabels) return;

        Label errorLabel = errorLabels.get(field);
        if (errorLabel == null) {
            // Create error label
            errorLabel = new Label();
            errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0;");
            errorLabel.setWrapText(true);
            errorLabels.put(field, errorLabel);

            // Try to insert after field
            if (field.getParent() instanceof Pane) {
                Pane parent = (Pane) field.getParent();
                int index = parent.getChildren().indexOf(field);
                if (index >= 0 && index < parent.getChildren().size()) {
                    parent.getChildren().add(index + 1, errorLabel);
                }
            }
        }

        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideErrorLabel(Node field) {
        Label errorLabel = errorLabels.get(field);
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void updateValidProperty() {
        valid.set(errors.isEmpty());
    }

    // ========================================================================
    // FIELD VALIDATOR CLASS
    // ========================================================================

    /**
     * Validator for a single field
     */
    @Getter
    public static class FieldValidator<T> {
        private final String fieldName;
        private final List<ValidationRule<T>> rules = new ArrayList<>();
        private java.util.function.Supplier<T> valueExtractor;
        private boolean required = false;
        private String requiredMessage = "This field is required";

        public FieldValidator(String fieldName) {
            this.fieldName = fieldName;
        }

        void setValueExtractor(java.util.function.Supplier<T> extractor) {
            this.valueExtractor = extractor;
        }

        // ---- Builder Methods ----

        /**
         * Mark field as required
         */
        public FieldValidator<T> required() {
            this.required = true;
            return this;
        }

        /**
         * Mark field as required with custom message
         */
        public FieldValidator<T> required(String message) {
            this.required = true;
            this.requiredMessage = message;
            return this;
        }

        /**
         * Add a custom validation rule
         */
        public FieldValidator<T> addRule(Predicate<T> condition, String errorMessage) {
            rules.add(new ValidationRule<>(condition, errorMessage));
            return this;
        }

        /**
         * Add a validation rule using a function that returns error message or null
         */
        public FieldValidator<T> addRule(Function<T, String> validator) {
            rules.add(new ValidationRule<>(validator));
            return this;
        }

        // ---- String-specific validations ----

        /**
         * Minimum length validation
         */
        @SuppressWarnings("unchecked")
        public FieldValidator<T> minLength(int min) {
            if (String.class.isAssignableFrom(getValueClass())) {
                addRule(value -> {
                    String str = (String) value;
                    return str == null || str.length() >= min ? null :
                            "Must be at least " + min + " characters";
                });
            }
            return this;
        }

        /**
         * Maximum length validation
         */
        @SuppressWarnings("unchecked")
        public FieldValidator<T> maxLength(int max) {
            if (String.class.isAssignableFrom(getValueClass())) {
                addRule(value -> {
                    String str = (String) value;
                    return str == null || str.length() <= max ? null :
                            "Must be at most " + max + " characters";
                });
            }
            return this;
        }

        /**
         * Pattern matching validation
         */
        @SuppressWarnings("unchecked")
        public FieldValidator<T> pattern(String regex, String errorMessage) {
            Pattern pattern = Pattern.compile(regex);
            addRule(value -> {
                String str = (String) value;
                return str == null || pattern.matcher(str).matches() ? null : errorMessage;
            });
            return this;
        }

        /**
         * Email format validation
         */
        public FieldValidator<T> email() {
            return pattern("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                    "Please enter a valid email address");
        }

        /**
         * Phone number validation
         */
        public FieldValidator<T> phone() {
            return pattern("^[\\d\\s\\-\\+\\(\\)]{10,}$",
                    "Please enter a valid phone number");
        }

        /**
         * Numeric only validation
         */
        public FieldValidator<T> numeric() {
            return pattern("^\\d+$", "Must contain only numbers");
        }

        /**
         * Alphabetic only validation
         */
        public FieldValidator<T> alphabetic() {
            return pattern("^[a-zA-Z\\s]+$", "Must contain only letters");
        }

        /**
         * Alphanumeric validation
         */
        public FieldValidator<T> alphanumeric() {
            return pattern("^[a-zA-Z0-9]+$", "Must contain only letters and numbers");
        }

        // ---- Number-specific validations ----

        /**
         * Minimum value validation
         */
        @SuppressWarnings("unchecked")
        public FieldValidator<T> min(Number min) {
            addRule(value -> {
                if (value == null) return null;
                if (value instanceof Number) {
                    return ((Number) value).doubleValue() >= min.doubleValue() ? null :
                            "Must be at least " + min;
                }
                return null;
            });
            return this;
        }

        /**
         * Maximum value validation
         */
        @SuppressWarnings("unchecked")
        public FieldValidator<T> max(Number max) {
            addRule(value -> {
                if (value == null) return null;
                if (value instanceof Number) {
                    return ((Number) value).doubleValue() <= max.doubleValue() ? null :
                            "Must be at most " + max;
                }
                return null;
            });
            return this;
        }

        // ---- Validation Execution ----

        /**
         * Run all validations
         */
        List<String> validate() {
            List<String> errors = new ArrayList<>();

            T value = valueExtractor != null ? valueExtractor.get() : null;

            // Required check
            if (required) {
                boolean isEmpty = value == null ||
                        (value instanceof String && ((String) value).trim().isEmpty());
                if (isEmpty) {
                    errors.add(requiredMessage);
                    return errors; // Don't run other validations if required and empty
                }
            }

            // Run other rules only if value is not empty
            if (value != null && !(value instanceof String && ((String) value).trim().isEmpty())) {
                for (ValidationRule<T> rule : rules) {
                    String error = rule.validate(value);
                    if (error != null) {
                        errors.add(error);
                    }
                }
            }

            return errors;
        }

        private Class<?> getValueClass() {
            if (valueExtractor != null) {
                T sample = valueExtractor.get();
                if (sample != null) return sample.getClass();
            }
            return Object.class;
        }
    }

    // ========================================================================
    // VALIDATION RULE CLASS
    // ========================================================================

    private static class ValidationRule<T> {
        private final Predicate<T> predicate;
        private final String errorMessage;
        private final Function<T, String> validator;

        ValidationRule(Predicate<T> predicate, String errorMessage) {
            this.predicate = predicate;
            this.errorMessage = errorMessage;
            this.validator = null;
        }

        ValidationRule(Function<T, String> validator) {
            this.validator = validator;
            this.predicate = null;
            this.errorMessage = null;
        }

        String validate(T value) {
            if (validator != null) {
                return validator.apply(value);
            } else if (predicate != null) {
                return predicate.test(value) ? null : errorMessage;
            }
            return null;
        }
    }

    // ========================================================================
    // VALIDATION ERROR CLASS
    // ========================================================================

    /**
     * Validation error holder
     */
    @Getter
    public static class ValidationError {
        private final String fieldName;
        private final String message;

        public ValidationError(String fieldName, String message) {
            this.fieldName = fieldName;
            this.message = message;
        }

        @Override
        public String toString() {
            return fieldName + ": " + message;
        }
    }

    // ========================================================================
    // UTILITY - VALIDATION SUMMARY BOX
    // ========================================================================

    /**
     * Create a validation summary box that displays all errors
     */
    public VBox createValidationSummary() {
        VBox summaryBox = new VBox(8);
        summaryBox.setPadding(new Insets(12));
        summaryBox.setStyle("-fx-background-color: #FEE2E2; -fx-background-radius: 8; -fx-border-color: #EF4444; -fx-border-radius: 8;");
        summaryBox.setVisible(false);
        summaryBox.setManaged(false);

        Label titleLabel = new Label("⚠ Please fix the following errors:");
        titleLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #991B1B;");

        VBox errorsList = new VBox(4);

        // Bind to errors
        errors.addListener((javafx.collections.ListChangeListener<ValidationError>) change -> {
            errorsList.getChildren().clear();

            if (errors.isEmpty()) {
                summaryBox.setVisible(false);
                summaryBox.setManaged(false);
            } else {
                for (ValidationError error : errors) {
                    Label errorLabel = new Label("• " + error.getFieldName() + ": " + error.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 12px;");
                    errorLabel.setWrapText(true);
                    errorsList.getChildren().add(errorLabel);
                }
                summaryBox.setVisible(true);
                summaryBox.setManaged(true);
            }
        });

        summaryBox.getChildren().addAll(titleLabel, errorsList);
        return summaryBox;
    }
}
