package com.back.devc.domain.post.comment.attachment.dto;

import java.time.LocalDateTime;

public record CommentAttachmentResponse(
        Long attachmentId,
        Long commentId,
        String fileName,
        String storedName,
        String fileUrl,
        String fileType,
        String mimeType,
        Long fileSize,
        Integer fileOrder,
        LocalDateTime createdAt
) {

    public Long getAttachmentId() {
        return attachmentId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStoredName() {
        return storedName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Integer getFileOrder() {
        return fileOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
