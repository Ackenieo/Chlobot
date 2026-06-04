package com.chlobot.platform.app.rag;

import java.util.Map;

public record IngestFolderRequest(String folderPath, Boolean recursive, Map<String, Object> metadata,
                                  String knowledgeBaseId) {
}
