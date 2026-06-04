package com.chlobot.platform.agent.model.springai;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StructuredOutputHelper {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public StructuredOutputHelper(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public <T> StructuredOutput<T> parse(String schemaName, String content, Class<T> outputType) {
        try {
            T data = objectMapper.readValue(stripMarkdownFence(content), outputType);
            validate(data);
            return new StructuredOutput<>(schemaName, data, Map.of("validated", true));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid structured output for schema: " + schemaName, ex);
        }
    }

    public <T> T convert(Object value, Class<T> outputType) {
        T data = objectMapper.convertValue(value, outputType);
        validate(data);
        return data;
    }

    private <T> void validate(T data) {
        Set<ConstraintViolation<T>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Structured output validation failed: " + message);
        }
    }

    private String stripMarkdownFence(String content) {
        if (content == null) {
            return "";
        }
        String trimmed = content.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstLine = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLine >= 0 && lastFence > firstLine) {
            return trimmed.substring(firstLine + 1, lastFence).trim();
        }
        return trimmed;
    }
}
