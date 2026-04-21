package com.back.devc.domain.post.comment.attachment.dto;

import java.util.List;

public record CommentAttachmentListResponse(
        List<CommentAttachmentResponse> attachments
) {

    public List<CommentAttachmentResponse> getAttachments() {
        return attachments;
    }
}