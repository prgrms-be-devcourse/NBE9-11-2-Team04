package com.back.devc.domain.post.comment.attachment.service;

import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentDeleteResponse;
import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentListResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommentAttachmentService {

    CommentAttachmentListResponse uploadAttachments(
            Long commentId,
            List<MultipartFile> files,
            List<Integer> fileOrders
    );

    CommentAttachmentListResponse getAttachments(Long commentId);

    CommentAttachmentDeleteResponse deleteAttachment(Long commentId, Long attachmentId);
}