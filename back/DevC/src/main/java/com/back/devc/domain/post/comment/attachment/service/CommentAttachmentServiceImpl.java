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
        // 첨부파일 처리 전에 댓글이 실제로 존재하는지, 그리고 soft delete 된 댓글이 아닌지 먼저 검증
        // 현재 댓글 기능은 soft delete 기반이라서 삭제된 댓글에는 첨부파일 업로드를 허용하지 않음
        Comment comment = validateComment(commentId);

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
        // 조회도 업로드/삭제와 동일하게 댓글 상태를 먼저 확인
        // 삭제된 댓글에 대해서는 첨부파일 목록 조회 역시 막음
        Comment comment = validateComment(commentId);

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
        // 삭제 전에 댓글 자체가 유효한지 먼저 확인
        // 존재하지 않거나 soft delete 된 댓글이면 첨부파일 삭제도 진행하지 않음
        Comment comment = validateComment(commentId);

        // validateComment(...) 에서 한 번 걸러지지만,
        // 삭제 로직은 영향도가 크기 때문에 "삭제된 댓글의 첨부파일은 삭제하지 않는다"는 의도를 한 번 더 명확히 남겨둠
        if (comment.isDeleted()) {
            throw new IllegalStateException("삭제된 댓글의 첨부파일은 삭제할 수 없습니다.");
        }
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

    /**
     * 첨부파일 처리 전 공통으로 사용하는 댓글 검증 메서드
     *
     * 이 서비스에서 업로드 / 조회 / 삭제를 할 때 모두 같은 기준으로 댓글을 확인해야 하므로,
     * 중복 코드를 줄이기 위해 공통 메서드로 분리
     *
     * 검증 내용
     * - commentId 에 해당하는 댓글이 실제로 존재하는지
     * - 현재 댓글이 soft delete 상태가 아닌지
     */
    private Comment validateComment(Long commentId) {
        // 먼저 commentId 로 실제 댓글을 조회하고, 없으면 바로 예외를 발생시킨다.
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다. id=" + commentId));

        // 현재 댓글 기능은 soft delete 기반으로 동작하므로,
        // 삭제된 댓글에 대해서는 첨부파일 업로드/조회/삭제를 모두 막는다.
        if (comment.isDeleted()) {
            throw new IllegalStateException("삭제된 댓글에는 첨부파일을 처리할 수 없습니다.");
        }

        return comment;
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