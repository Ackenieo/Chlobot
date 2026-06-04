package com.chlobot.platform.app.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpringAiRagAdapter {

    private static final int DIMENSIONS = 1024;
    private final EmbeddingModel embeddingModel;

    public SpringAiRagAdapter(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] embed(String text) {
        return embeddingModel.embed(text == null ? "" : text);
    }

    public List<Float> asList(float[] values) {
        List<Float> result = new ArrayList<>(values.length);
        for (float value : values) {
            result.add(value);
        }
        return result;
    }

    public String vectorStoreMode() {
        return "spring-ai-embedding-model-with-pgvector-compatible-jdbc-store";
    }

    public static float[] deterministicEmbedding(String value) {
        byte[] bytes = (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
        float[] vector = new float[DIMENSIONS];
        for (int i = 0; i < bytes.length; i++) {
            vector[i % DIMENSIONS] += (bytes[i] & 0xff) / 255.0f;
        }
        return vector;
    }
}
