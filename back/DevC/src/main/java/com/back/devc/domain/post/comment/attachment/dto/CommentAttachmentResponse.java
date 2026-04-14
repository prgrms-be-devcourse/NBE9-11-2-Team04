package com.back.devc.domain.post.comment.attachment.dto;

import java.time.LocalDateTime;

public class CommentAttachmentResponse {

    private final Long attachmentId;
    private final Long commentId;
    private final String fileName;
    private final String storedName;
    private final String fileUrl;
    private final String fileType;
    private final String mimeType;
    private final Long fileSize;
    private final Integer fileOrder;
    private final LocalDateTime createdAt;

    public CommentAttachmentResponse(
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
        this.attachmentId = attachmentId;
        this.commentId = commentId;
        this.fileName = fileName;
        this.storedName = storedName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.fileOrder = fileOrder;
        this.createdAt = createdAt;
    }

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
