package com.chlobot.platform.app.rag;

import java.util.Map;

public record RagSearchResult(String documentId, String chunkId, String title, String content, double score,
                              Map<String, Object> metadata) {
}
