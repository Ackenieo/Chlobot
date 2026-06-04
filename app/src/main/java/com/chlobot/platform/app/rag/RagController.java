package com.chlobot.platform.app.rag;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;
    private final KnowledgeBaseService knowledgeBaseService;

    public RagController(RagService ragService, KnowledgeBaseService knowledgeBaseService) {
        this.ragService = ragService;
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @Operation(summary = "List knowledge bases")
    @GetMapping("/knowledge-bases")
    public List<KnowledgeBase> listKnowledgeBases() {
        return knowledgeBaseService.findAll();
    }

    @Operation(summary = "Create a knowledge base")
    @PostMapping("/knowledge-bases")
    public KnowledgeBase createKnowledgeBase(@RequestBody CreateKnowledgeBaseRequest request) {
        return knowledgeBaseService.create(request);
    }

    @Operation(summary = "Update a knowledge base")
    @PatchMapping("/knowledge-bases/{knowledgeBaseId}")
    public KnowledgeBase updateKnowledgeBase(@PathVariable String knowledgeBaseId, @RequestBody UpdateKnowledgeBaseRequest request) {
        return knowledgeBaseService.update(knowledgeBaseId, request);
    }

    @Operation(summary = "Delete a knowledge base")
    @DeleteMapping("/knowledge-bases/{knowledgeBaseId}")
    public Map<String, Object> deleteKnowledgeBase(@PathVariable String knowledgeBaseId) {
        knowledgeBaseService.delete(knowledgeBaseId);
        return Map.of("knowledgeBaseId", knowledgeBaseId, "deleted", true);
    }

    @Operation(summary = "List documents in a knowledge base")
    @GetMapping("/knowledge-bases/{knowledgeBaseId}/documents")
    public List<DocumentEmbedding> listKnowledgeBaseDocuments(@PathVariable String knowledgeBaseId) {
        return ragService.listDocuments(knowledgeBaseId);
    }

    @Operation(summary = "Ingest a document for RAG")
    @PostMapping("/documents")
    public DocumentEmbedding ingest(@RequestBody IngestDocumentRequest request) {
        return ragService.ingest(request);
    }

    @Operation(summary = "Ingest dropped files for RAG")
    @PostMapping(value = "/documents/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<DocumentEmbedding> ingestFiles(@RequestPart("files") List<MultipartFile> files,
                                                @RequestParam(required = false) String knowledgeBaseId) {
        return files.stream()
                .filter(file -> !file.isEmpty())
                .filter(file -> ragService.isSupportedTextFile(Path.of(file.getOriginalFilename() == null ? "" : file.getOriginalFilename())))
                .map(file -> toIngestRequest(file, knowledgeBaseId))
                .map(ragService::ingest)
                .toList();
    }

    @Operation(summary = "Ingest server-side folder for RAG")
    @PostMapping("/documents/folder")
    public List<DocumentEmbedding> ingestFolder(@RequestBody IngestFolderRequest request) {
        return ragService.ingestFolder(request);
    }

    private IngestDocumentRequest toIngestRequest(MultipartFile file, String knowledgeBaseId) {
        try {
            String filename = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                    ? "uploaded-document"
                    : file.getOriginalFilename();
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (content.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("Unsupported binary file: " + filename);
            }
            return new IngestDocumentRequest(filename, content, filename,
                    Map.of("source", "file-upload", "filename", filename, "size", file.getSize()), knowledgeBaseId);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read uploaded file", ex);
        }
    }

    @Operation(summary = "Search documents")
    @PostMapping("/search")
    public List<RagSearchResult> search(@RequestBody RagSearchRequest request) {
        return ragService.search(request);
    }

    @Operation(summary = "Get document details")
    @GetMapping("/documents/{documentId}")
    public String getDocument(@PathVariable String documentId) {
        return documentId;
    }

    @Operation(summary = "Delete a document")
    @DeleteMapping("/documents/{documentId}")
    public Map<String, Object> deleteDocument(@PathVariable String documentId) {
        ragService.deleteDocument(documentId);
        return Map.of("documentId", documentId, "deleted", true);
    }

    @Operation(summary = "Get Java-native RAG evaluation metrics")
    @GetMapping("/evaluation")
    public Map<String, Object> evaluation(@RequestParam String query) {
        return ragService.evaluation(query);
    }
}
