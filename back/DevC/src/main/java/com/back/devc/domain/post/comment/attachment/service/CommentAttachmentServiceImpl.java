package com.back.devc.domain.post.comment.attachment.service;

import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentDeleteResponse;
import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentListResponse;
import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentResponse;
import com.back.devc.domain.post.comment.attachment.entity.CommentAttachment;
import com.back.devc.domain.post.comment.attachment.repository.CommentAttachmentRepository;
import com.back.devc.domain.post.comment.entity.Comment;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentAttachmentServiceImpl implements CommentAttachmentService {

    private static final Path UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads", "comments");

    private final CommentAttachmentRepository commentAttachmentRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentAttachmentListResponse uploadAttachments(
            Long commentId,
            List<MultipartFile> files,
            List<Integer> fileOrders
    ) {
        validateCommentExists(commentId);

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        try {
            Path uploadPath = UPLOAD_DIR;
            Files.createDirectories(uploadPath);

            List<CommentAttachmentResponse> responses = new ArrayList<>();

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);

                if (file.isEmpty()) {
                    continue;
                }

                String originalFilename = StringUtils.cleanPath(
                        file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()
                );

                String storedName = UUID.randomUUID() + "_" + originalFilename;
                Path targetPath = uploadPath.resolve(storedName);

                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }

                String mimeType = file.getContentType() == null
                        ? "application/octet-stream"
                        : file.getContentType();

                String fileType = mimeType.startsWith("image/") ? "IMAGE" : "FILE";

                Integer fileOrder = (fileOrders != null && fileOrders.size() > i)
                        ? fileOrders.get(i)
                        : i + 1;

                CommentAttachment attachment = CommentAttachment.create(
                        commentId,
                        originalFilename,
                        storedName,
                        "/uploads/comments/" + storedName,
                        fileType,
                        mimeType,
                        file.getSize(),
                        fileOrder
                );

                CommentAttachment savedAttachment = commentAttachmentRepository.save(attachment);
                responses.add(toResponse(savedAttachment));
            }

            return new CommentAttachmentListResponse(responses);
        } catch (IOException e) {
            throw new RuntimeException("댓글 첨부파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public CommentAttachmentListResponse getAttachments(Long commentId) {
        validateCommentExists(commentId);

        List<CommentAttachmentResponse> attachments = commentAttachmentRepository
                .findByCommentIdOrderByFileOrderAscIdAsc(commentId)
                .stream()
                .map(this::toResponse)
                .toList();

        return new CommentAttachmentListResponse(attachments);
    }

    @Override
    @Transactional
    public CommentAttachmentDeleteResponse deleteAttachment(Long commentId, Long attachmentId) {
        validateCommentExists(commentId);

        CommentAttachment attachment = commentAttachmentRepository.findByIdAndCommentId(attachmentId, commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글 첨부파일을 찾을 수 없습니다. id=" + attachmentId));

        try {
            Path filePath = UPLOAD_DIR.resolve(attachment.getStoredName());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("댓글 첨부파일 삭제 중 오류가 발생했습니다.", e);
        }

        commentAttachmentRepository.delete(attachment);

        return new CommentAttachmentDeleteResponse(
                attachmentId,
                "댓글 첨부파일이 삭제되었습니다."
        );
    }

    private void validateCommentExists(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다. id=" + commentId));

        if (comment.isDeleted()) {
            throw new IllegalStateException("삭제된 댓글에는 첨부파일을 처리할 수 없습니다.");
        }
    }

    private CommentAttachmentResponse toResponse(CommentAttachment attachment) {
        return new CommentAttachmentResponse(
                attachment.getId(),
                attachment.getCommentId(),
                attachment.getFileName(),
                attachment.getStoredName(),
                attachment.getFileUrl(),
                attachment.getFileType(),
                attachment.getMimeType(),
                attachment.getFileSize(),
                attachment.getFileOrder(),
                attachment.getCreatedAt()
        );
    }
}