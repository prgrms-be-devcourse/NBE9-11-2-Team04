package com.back.devc.domain.post.comment.attachment.dto;

import java.util.List;

public class CommentAttachmentListResponse {

    private final List<CommentAttachmentResponse> attachments;

    public CommentAttachmentListResponse(List<CommentAttachmentResponse> attachments) {
        this.attachments = attachments;
    }

    public List<CommentAttachmentResponse> getAttachments() {
        return attachments;
    }
}