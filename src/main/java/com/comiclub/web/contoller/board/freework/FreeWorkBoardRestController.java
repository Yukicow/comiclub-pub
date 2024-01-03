package com.comiclub.web.contoller.board.freework;

import com.comiclub.domain.entity.board.FreeWorkBoardComment;
import com.comiclub.domain.repository.board.freework.FWBCommentRepository;
import com.comiclub.web.result.CommonResult;
import com.comiclub.domain.service.board.FWBoardService;
import com.comiclub.web.contoller.common.dto.CommentDto;
import com.comiclub.web.contoller.common.dto.CommentSaveDto;
import com.comiclub.web.contoller.common.dto.DecreaseInfo;
import com.comiclub.web.exception.NoAuthorizationException;
import com.comiclub.web.exception.NotFoundException;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import static com.comiclub.domain.util.constant.CommonConst.*;

@Slf4j
@RestController
@RequestMapping("/fwBoards")
@RequiredArgsConstructor
public class FreeWorkBoardRestController {


    private final FWBoardService fwBoardService;
    private final FWBCommentRepository fwbCommentRepository;




    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardId}/like")
    public ResponseEntity likeBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long boardId) {

        fwBoardService.likeBoard(boardId, memberInfo.getId());

        return ResponseEntity
                .ok()
                .body(new CommonResult<String>(HttpServletResponse.SC_OK, "SUCCESS", null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardId}/comment")
    public ResponseEntity<CommonResult> createComment(@CurrentMember LoginMember memberInfo,
                                                      @PathVariable Long boardId, @RequestBody String content) {

        if(!StringUtils.hasText(content) || content.length() > MAX_COMMENT_LENGTH)
            throw new ValidationException("Content is empty or over length 1000");

        CommentSaveDto saveDto = new CommentSaveDto(
                memberInfo.getId(),
                boardId,
                null,
                memberInfo.getNickname(),
                content
        );

        CommentDto commentDto = fwBoardService.saveComment(saveDto);

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", commentDto));
    }

    @GetMapping("/{boardId}/comments")
    public ResponseEntity<CommonResult> fetchComments(@PathVariable Long boardId,
                                                      @PageableDefault(size = 15, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Slice<CommentDto> comments = fwBoardService.getSliceOfMoreComments(boardId, null, pageable);

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", comments.getContent()));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommonResult> updateComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId,
                                        @RequestBody String content) {

        if(!StringUtils.hasText(content) || content.length() > MAX_COMMENT_LENGTH)
            throw new ValidationException("Content is empty or over length 1000");

        CommentDto commentDto = fwBoardService.updateComment(commentId, memberInfo.getId(), content);

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", commentDto));
    }


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<CommonResult> deleteComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        DecreaseInfo decreaseInfo = fwBoardService.deleteComment(commentId, memberInfo.getId());

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", decreaseInfo));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<CommonResult> likeComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        String code = fwBoardService.likeComment(commentId, memberInfo.getId());

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, code, null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comments/{commentId}/unlike")
    public ResponseEntity<CommonResult> unlikeComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        String code = fwBoardService.unlikeComment(commentId, memberInfo.getId());

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, code, null));
    }

    @GetMapping("/{boardId}/comments/{commentId}/replies")
    public ResponseEntity<CommonResult> fetchReplies(@PathVariable Long boardId, @PathVariable Long commentId,
                                                     @PageableDefault(size = 2, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Slice<CommentDto> comments = fwBoardService.getSliceOfMoreComments(boardId, commentId, pageable);

        String code = "NO_NEXT";
        if (comments.hasNext()) code = "HAS_NEXT";

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, code, comments.getContent()));
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardId}/comments/{commentId}/reply")
    public ResponseEntity<CommonResult> writeReply(@CurrentMember LoginMember memberInfo,
                                                   @PathVariable Long boardId, @PathVariable Long commentId,
                                                   @RequestBody String content) {

        if(!StringUtils.hasText(content) || content.length() > MAX_COMMENT_LENGTH)
            throw new ValidationException("Content is empty or over length 1000");

        CommentSaveDto saveDto = new CommentSaveDto(
                memberInfo.getId(),
                boardId,
                commentId,
                memberInfo.getNickname(),
                content
        );

        CommentDto commentDto = fwBoardService.saveReply(saveDto);

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", commentDto));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/comments/{commentId}/auth")
    public ResponseEntity<CommonResult> authComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        FreeWorkBoardComment comment = fwbCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment with id '" + commentId + "'"));

        if(!comment.getMemberId().equals(memberInfo.getId()))
            throw new NoAuthorizationException("No Authorization for updating comment");

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", null));
    }





}
