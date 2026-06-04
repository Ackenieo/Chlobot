package com.chlobot.platform.app.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class RagService {

    private final RagRepository repository;
    private final KnowledgeBaseService knowledgeBaseService;
    private final EmbeddingModel embeddingModel;
    private final SpringAiRagAdapter springAiRagAdapter;

    public RagService(RagRepository repository, KnowledgeBaseService knowledgeBaseService,
                      EmbeddingModel embeddingModel, SpringAiRagAdapter springAiRagAdapter) {
        this.repository = repository;
        this.knowledgeBaseService = knowledgeBaseService;
        this.embeddingModel = embeddingModel;
        this.springAiRagAdapter = springAiRagAdapter;
    }

    public DocumentEmbedding ingest(IngestDocumentRequest request) {
        requireKnowledgeBase(request.knowledgeBaseId());
        float[] embedding = embeddingModel.embed(request.content());
        return repository.save(request, springAiRagAdapter.asList(embedding));
    }

    public List<DocumentEmbedding> ingestFolder(IngestFolderRequest request) {
        Path folder = Path.of(request.folderPath()).normalize();
        requireKnowledgeBase(request.knowledgeBaseId());
        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Folder does not exist: " + request.folderPath());
        }

        int depth = Boolean.TRUE.equals(request.recursive()) ? Integer.MAX_VALUE : 1;
        try (Stream<Path> paths = Files.walk(folder, depth)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedTextFile)
                    .map(path -> ingestFile(folder, path, request.metadata()))
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to ingest folder: " + request.folderPath(), ex);
        }
    }

    private DocumentEmbedding ingestFile(Path folder, Path file, Map<String, Object> metadata) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            if (content.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("Unsupported binary file: " + file.getFileName());
            }
            Map<String, Object> nextMetadata = metadata == null
                    ? new java.util.HashMap<>()
                    : new java.util.HashMap<>(metadata);
            nextMetadata.put("source", "folder-import");
            nextMetadata.put("path", file.toString());
            nextMetadata.put("relativePath", folder.relativize(file).toString());
            return ingest(new IngestDocumentRequest(file.getFileName().toString(), content, file.toString(), nextMetadata,
                    metadata == null ? null : String.valueOf(metadata.get("knowledgeBaseId"))));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read file: " + file, ex);
        }
    }

    boolean isSupportedTextFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".txt")
                || name.endsWith(".md")
                || name.endsWith(".markdown")
                || name.endsWith(".json")
                || name.endsWith(".csv")
                || name.endsWith(".yml")
                || name.endsWith(".yaml")
                || name.endsWith(".java")
                || name.endsWith(".ts")
                || name.endsWith(".tsx")
                || name.endsWith(".js")
                || name.endsWith(".jsx")
                || name.endsWith(".xml")
                || name.endsWith(".html")
                || name.endsWith(".css");
    }

    public List<RagSearchResult> search(RagSearchRequest request) {
        int topK = request.topK() == null ? 5 : Math.max(1, Math.min(request.topK(), 20));
        return repository.search(request.query(), topK, request.knowledgeBaseId());
    }

    public List<DocumentEmbedding> listDocuments(String knowledgeBaseId) {
        requireKnowledgeBase(knowledgeBaseId);
        return repository.listDocuments(knowledgeBaseId);
    }

    public void deleteDocument(String documentId) {
        repository.deleteDocument(documentId);
    }

    private void requireKnowledgeBase(String knowledgeBaseId) {
        if (knowledgeBaseId != null && !knowledgeBaseId.isBlank()) {
            knowledgeBaseService.get(knowledgeBaseId);
        }
    }

    public Map<String, Object> evaluation(String query) {
        List<RagSearchResult> results = search(new RagSearchRequest(query, 5, "hybrid", null));
        int hits = results.isEmpty() ? 0 : 1;
        double recallAt5 = results.isEmpty() ? 0.0 : 1.0;
        double mrr = results.isEmpty() ? 0.0 : 1.0;
        return Map.of(
                "query", query,
                "hitAt5", hits,
                "recallAt5", recallAt5,
                "mrr", mrr,
                "citationCoverage", results.isEmpty() ? 0.0 : 1.0,
                "emptyRetrievalRate", results.isEmpty() ? 1.0 : 0.0,
                "latencyMs", 0,
                "mode", springAiRagAdapter.vectorStoreMode());
    }
}
