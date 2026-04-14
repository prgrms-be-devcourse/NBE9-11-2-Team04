package com.back.devc.domain.post.comment.attachment.dto;

public class CommentAttachmentDeleteResponse {

    private final Long attachmentId;
    private final String message;

    public CommentAttachmentDeleteResponse(Long attachmentId, String message) {
        this.attachmentId = attachmentId;
        this.message = message;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public String getMessage() {
        return message;
    }
}