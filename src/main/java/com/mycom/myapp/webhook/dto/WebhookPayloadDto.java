package com.mycom.myapp.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class WebhookPayloadDto {

    @Getter @Setter @ToString
    public static class PushPayload {
        private String ref;
        private List<CommitInfo> commits;
        private RepositoryInfo repository;
    }

    @Getter
    @Setter
    @ToString
    public static class CommitInfo {
        private String id; // Commit SHA
        private String message;
        private AuthorInfo author;
        private ZonedDateTime timestamp;
        private List<String> added;
        private List<String> removed;
        private List<String> modified;
    }

    @Getter @Setter @ToString
    public static class AuthorInfo {
        private String name;
        private String email;
    }

    @Getter @Setter @ToString
    public static class RepositoryInfo {
        @JsonProperty("full_name")
        private String fullName;
        @JsonProperty("html_url")
        private String htmlUrl;
    }
}
