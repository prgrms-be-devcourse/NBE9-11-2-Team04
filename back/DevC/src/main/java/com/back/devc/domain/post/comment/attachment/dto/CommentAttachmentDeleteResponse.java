package com.back.devc.domain.post.comment.attachment.dto;

public record CommentAttachmentDeleteResponse(
        Long attachmentId,
        String message
) {

    public Long getAttachmentId() {
        return attachmentId;
    }

    public String getMessage() {
        return message;
    }
}