package com.chlobot.platform.app.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class DeterministicEmbeddingModel implements EmbeddingModel {

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = request.getInstructions().stream()
                .map(text -> new Embedding(SpringAiRagAdapter.deterministicEmbedding(text), 0))
                .toList();
        return new EmbeddingResponse(embeddings, new EmbeddingResponseMetadata("deterministic-java", null));
    }

    @Override
    public float[] embed(String text) {
        return SpringAiRagAdapter.deterministicEmbedding(text);
    }

    @Override
    public float[] embed(Document document) {
        return SpringAiRagAdapter.deterministicEmbedding(document == null ? "" : document.getText());
    }

    @Override
    public int dimensions() {
        return 1024;
    }
}
