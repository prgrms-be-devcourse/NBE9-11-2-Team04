package com.back.devc.domain.post.comment.attachment.service;

import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentDeleteResponse;
import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentListResponse;
import com.back.devc.domain.post.comment.attachment.dto.CommentAttachmentResponse;
import com.back.devc.domain.post.comment.attachment.entity.CommentAttachment;
import com.back.devc.domain.post.comment.attachment.repository.CommentAttachmentRepository;
import com.back.devc.domain.post.comment.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentAttachmentService {

    private final CommentAttachmentRepository commentAttachmentRepository;
    private final CommentRepository commentRepository;
    private static final Path COMMENT_UPLOAD_DIR = Paths.get("uploads", "comments");

    @Transactional
    public CommentAttachmentListResponse uploadAttachments(
            Long commentId,
            List<MultipartFile> files,
            List<Integer> fileOrders
    ) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다. id=" + commentId));

        if (files == null || files.isEmpty()) {
            return new CommentAttachmentListResponse(List.of());
        }

        List<CommentAttachmentResponse> responses = new java.util.ArrayList<>();

        for (int index = 0; index < files.size(); index++) {
            MultipartFile file = files.get(index);
            Integer fileOrder = (fileOrders != null && fileOrders.size() > index)
                    ? fileOrders.get(index)
                    : index;

            String originalFilename = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "unnamed";
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : "application/octet-stream";
            String extension = extractExtension(originalFilename);
            String storedName = UUID.randomUUID() + extension;
            String fileType = contentType.startsWith("image/") ? "IMAGE" : "FILE";
            saveFile(file, storedName);
            String fileUrl = "/uploads/comments/" + storedName;

            CommentAttachment attachment = CommentAttachment.create(
                    commentId,
                    originalFilename,
                    storedName,
                    fileUrl,
                    fileType,
                    contentType,
                    file.getSize(),
                    fileOrder
            );

            CommentAttachment savedAttachment = commentAttachmentRepository.save(attachment);

            responses.add(new CommentAttachmentResponse(
                    savedAttachment.getId(),
                    savedAttachment.getCommentId(),
                    savedAttachment.getFileName(),
                    savedAttachment.getStoredName(),
                    savedAttachment.getFileUrl(),
                    savedAttachment.getFileType(),
                    savedAttachment.getMimeType(),
                    savedAttachment.getFileSize(),
                    savedAttachment.getFileOrder(),
                    savedAttachment.getCreatedAt()
            ));
        }

        return new CommentAttachmentListResponse(responses);
    }

    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex);
    }

    private void saveFile(MultipartFile file, String storedName) {
        try {
            Files.createDirectories(COMMENT_UPLOAD_DIR);
            Path targetPath = COMMENT_UPLOAD_DIR.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("댓글 첨부파일 저장에 실패했습니다.", e);
        }
    }

    private void deleteFileIfExists(String storedName) {
        try {
            Path targetPath = COMMENT_UPLOAD_DIR.resolve(storedName);
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            throw new UncheckedIOException("댓글 첨부파일 삭제에 실패했습니다.", e);
        }
    }

    public CommentAttachmentListResponse getAttachments(Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다. id=" + commentId));

        List<CommentAttachmentResponse> responses = commentAttachmentRepository.findByCommentIdOrderByFileOrderAscIdAsc(commentId)
                .stream()
                .map(attachment -> new CommentAttachmentResponse(
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
                ))
                .toList();

        return new CommentAttachmentListResponse(responses);
    }

    @Transactional
    public CommentAttachmentDeleteResponse deleteAttachment(Long commentId, Long attachmentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다. id=" + commentId));

        CommentAttachment attachment = commentAttachmentRepository.findByIdAndCommentId(attachmentId, commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글 첨부파일을 찾을 수 없습니다. id=" + attachmentId));

        deleteFileIfExists(attachment.getStoredName());
        commentAttachmentRepository.delete(attachment);
        return new CommentAttachmentDeleteResponse(attachmentId, "댓글 첨부파일 삭제 성공");
    }
}
