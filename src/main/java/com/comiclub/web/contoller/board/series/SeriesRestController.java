package com.comiclub.web.contoller.board.series;


import com.comiclub.web.result.CommonResult;
import com.comiclub.domain.service.board.EpBoardService;
import com.comiclub.domain.service.board.SeriesService;
import com.comiclub.web.contoller.common.dto.CommentDto;
import com.comiclub.web.contoller.common.dto.CommentSaveDto;
import com.comiclub.web.contoller.common.dto.DecreaseInfo;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


import static com.comiclub.domain.util.constant.CommonConst.*;

@RestController
@RequestMapping("/series")
@RequiredArgsConstructor
public class SeriesRestController {


    private final SeriesService seriesService;
    private final EpBoardService epBoardService;


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{seriesId}/follow")
    public ResponseEntity<CommonResult> followSeries(@CurrentMember LoginMember memberInfo, @PathVariable Long seriesId) {

        String follow = seriesService.followSeries(memberInfo.getId(), seriesId, memberInfo.getAdult());

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", follow));
    }



    @PreAuthorize("isAuthenticated()")
    @PostMapping("/epBoards/{boardId}/like")
    public ResponseEntity likeBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long boardId) {

        String code = epBoardService.likeBoard(boardId, memberInfo.getId());

        return ResponseEntity
                .ok()
                .body(new CommonResult<String>(HttpServletResponse.SC_OK, code, null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/epBoards/{boardId}/comment")
    public ResponseEntity<CommonResult> writeComment(@CurrentMember LoginMember memberInfo, @PathVariable Long boardId,
                                                     @RequestBody String content) {

        if(!StringUtils.hasText(content) || content.length() > MAX_COMMENT_LENGTH)
            throw new ValidationException("Content is empty or over length 1000");

        CommentSaveDto commentSaveDto = new CommentSaveDto(
                memberInfo.getId(),
                boardId,
                null,
                memberInfo.getNickname(),
                content
        );

        CommentDto comment = epBoardService.saveComment(commentSaveDto);

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", comment));
    }

    @GetMapping("/epBoards/{boardId}/comments")
    public ResponseEntity<CommonResult> fetchComments(@PathVariable Long boardId,
                                                      @PageableDefault(size = 15, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Slice<CommentDto> slice = epBoardService.getSliceOfMoreComments(boardId, null, pageable);
        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", slice.getContent()));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/epBoards/comments/{commentId}")
    public ResponseEntity<CommonResult> modifyComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId, @RequestBody String content) {

        if(!StringUtils.hasText(content) || content.length() > MAX_COMMENT_LENGTH)
            throw new ValidationException("Content is empty or over length 1000");

        epBoardService.updateComment(commentId, memberInfo.getId(), content);

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", null));
    }


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/epBoards/comments/{commentId}")
    public ResponseEntity<CommonResult> deleteComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        DecreaseInfo decreaseInfo = epBoardService.deleteComment(commentId, memberInfo.getId());

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", decreaseInfo));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/epBoards/comments/{commentId}/like")
    public ResponseEntity<CommonResult> likeComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        String code = epBoardService.likeComment(commentId, memberInfo.getId());

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, code, null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/epBoards/comments/{commentId}/unlike")
    public ResponseEntity<CommonResult> unlikeComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        String code = epBoardService.unlikeComment(commentId, memberInfo.getId());

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, code, null));
    }

    @GetMapping("/epBoards/{boardId}/comments/{commentId}/replies")
    public ResponseEntity<CommonResult> fetchReplies(@PathVariable Long boardId, @PathVariable Long commentId,
                                                     @PageableDefault(size = 15, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Slice<CommentDto> slice = epBoardService.getSliceOfMoreComments(boardId, commentId, pageable);

        String code = slice.hasNext()
                ? "HAS_NEXT"
                : "NO_NEXT";

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, code, slice.getContent()));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/epBoards/{boardId}/comments/{commentId}/reply")
    public ResponseEntity<CommonResult> writeReply(@CurrentMember LoginMember memberInfo, @PathVariable Long boardId,
                                                   @PathVariable Long commentId, @RequestBody String content) {

        if(!StringUtils.hasText(content) || content.length() > MAX_COMMENT_LENGTH)
            throw new ValidationException("Content is empty or over length 1000");

        CommentSaveDto commentSaveDto = new CommentSaveDto(
                memberInfo.getId(),
                boardId,
                commentId,
                memberInfo.getNickname(),
                content
        );

        CommentDto comment = epBoardService.saveReply(commentSaveDto);

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", comment));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/epBoards/comments/{commentId}/auth")
    public ResponseEntity<CommonResult> authComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        epBoardService.authComment(commentId, memberInfo.getId());

        return ResponseEntity
                .ok()
                .body(new CommonResult<CommentDto>(HttpServletResponse.SC_OK, "SUCCESS", null));
    }

}
