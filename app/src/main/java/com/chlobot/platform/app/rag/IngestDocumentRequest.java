package com.chlobot.platform.app.rag;

import java.util.Map;

public record IngestDocumentRequest(String title, String content, String sourceUri, Map<String, Object> metadata,
                                    String knowledgeBaseId) {
}
