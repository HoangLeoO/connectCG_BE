package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.AiModerationResult;

public interface AiModerationService {
    AiModerationResult checkPostContent(String content);
}
