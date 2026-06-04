package com.chlobot.platform.app.rag;

public record RagSearchRequest(String query, Integer topK, String mode, String knowledgeBaseId) {
}
