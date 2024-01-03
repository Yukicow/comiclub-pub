package com.comiclub.web.contoller.club;

import com.comiclub.domain.entity.board.enumtype.Topic;
import com.comiclub.domain.entity.member.Role;
import com.comiclub.web.contoller.common.dto.CommentDto;
import com.comiclub.domain.entity.board.*;
import com.comiclub.web.contoller.common.dto.CommentSaveDto;
import com.comiclub.web.contoller.common.dto.DecreaseInfo;
import com.comiclub.web.exception.NoAuthenticationException;
import com.comiclub.web.exception.NoAuthorizationException;
import com.comiclub.web.exception.NotFoundException;
import com.comiclub.web.contoller.club.form.FreeBoardForm;
import com.comiclub.domain.repository.board.free.FreeBoardRepository;
import com.comiclub.domain.repository.board.free.FBCommentRepository;
import com.comiclub.web.result.CommonResult;
import com.comiclub.domain.service.board.FreeBoardService;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.comiclub.domain.util.constant.CommonConst.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClubRestController {


    private final FreeBoardRepository boardRepository;
    private final FBCommentRepository commentRepository;
    private final FreeBoardService boardService;


    @PostMapping("/clubs/{clubId}/board")
    public ResponseEntity<CommonResult> writeFreeBoard(
            @Validated @ModelAttribute("board") FreeBoardForm form, BindingResult bindingResult,
            @PathVariable("clubId") Long clubId, @CurrentMember LoginMember memberInfo) {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Validation error has been occurred");
        }

        // 로그인한 유저인 지
        boolean authMember = memberInfo != null;

        boolean isAdmin = authMember && memberInfo.getRole().equals(Role.ADMIN);
        // Admin이 아니면서 공지를 작성하는 요청이 때
        if( !isAdmin && form.getTopic().equals(Topic.NOTICE) )
            throw new NoAuthorizationException("No authorization for writing notice");

        boolean isAdult = authMember && memberInfo.getAdult();
        // 성인 인증이 안 되어 있는데 성인글을 게시하려는 경우
        if( !isAdult && form.getAdultOnly() )
            throw new NoAuthorizationException("No authorization for writing adult only board");


        FreeBoard freeBoard = FreeBoard.createNewFreeBoard(
                authMember ? memberInfo.getId() : null,
                clubId,
                form.getTopic(),
                form.getWriter(),
                form.getPassword(),
                form.getTitle(),
                form.getContent(),
                form.getAdultOnly()
        );

        // 로그인 유저일 경우 password는 null 작성자는 nickname으로 초기화
        if(authMember){
            freeBoard.changePassword(null);
            freeBoard.changeWriter(memberInfo.getNickname());
        }

        boardService.saveBoard(freeBoard);
        return ResponseEntity.ok(new CommonResult(HttpStatus.OK.value(), "REDIRECT", "/clubs/" + clubId));
    }

    @PatchMapping("/clubs/{clubId}/boards/{boardId}")
    public ResponseEntity editFreeBoard(@PathVariable Long clubId, @PathVariable Long boardId, @CurrentMember LoginMember memberInfo,
                                        @ModelAttribute("board") FreeBoardForm form, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response){

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Validation error has been occurred");
        }

        FreeBoard freeBoard = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no board id '" + boardId + "'"));

        boolean authMember = memberInfo != null; // 로그인한 사용자인지
        boolean writtenByAuthMember = freeBoard.getMemberId() != null; // 게시글이 인증된 사용자가 등록한 게시글인지

        if(writtenByAuthMember && authMember){ // 로그인한 사용자가 등록한 게시글이면서 로그인 유저인 경우
            boolean sameWriter = freeBoard.getMemberId().equals(memberInfo.getId());
            if(!sameWriter)
                throw new NoAuthorizationException("No authorization for modifying comment");
        }
        else{
            if(request.getCookies() == null)
                throw new NoAuthorizationException("No authorization for modifying board");

            Cookie passwordCookie = Arrays.stream(request.getCookies())
                    .filter((cookie) -> cookie.getName().equals("FB" + boardId))
                    .findFirst()
                    .orElseThrow(() -> new NoAuthenticationException("No authenticationException for modifying comment"));

            boolean matchPassword = freeBoard.getPassword().equals(passwordCookie.getValue()); // password가 일치하는지
            if(!matchPassword)
                throw new NoAuthenticationException("No authenticationException for modifying comment");
            else {
                passwordCookie.setPath("/clubs/" + clubId + "/boards/" + boardId);
                passwordCookie.setMaxAge(0);
                response.addCookie(passwordCookie);
            }
        }

        boardService.updateFreeBoard(boardId, form);
        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "SUCCESS", null));
    }
    
    @PostMapping("/clubs/{clubId}/boards/{boardId}/auth")
    public ResponseEntity authFreeBoard(@CurrentMember LoginMember memberInfo,  @PathVariable Long clubId, @PathVariable Long boardId, @RequestBody(required = false) String password, HttpServletResponse response){

        FreeBoard freeBoard = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no board id '" + boardId + "'"));

        boolean authMember = memberInfo != null; // 인증된 사용자인지
        boolean writtenByAuthMember = freeBoard.getMemberId() != null; // 게시글이 인증된 사용자가 등록한 게시글인지

        if(writtenByAuthMember && authMember){ // 인증된 사용자가 등록한 게시글이면서 로그인 유저인 경우
            boolean sameWriter = freeBoard.getMemberId().equals(memberInfo.getId());
            if(!sameWriter)
                throw new NoAuthorizationException("No authorization");
        }
        else if(StringUtils.hasText(password)){  // 인증되지 않은 사용자가 등록한 게시글일 경우
            boolean matchPassword = freeBoard.getPassword().equals(password);
            if (!matchPassword){
                throw new NoAuthorizationException("No authorization");
            }
            else{
                Cookie cookie = new Cookie("FB" + boardId, password);
                cookie.setPath("/clubs/" + clubId + "/boards/" + boardId);
                cookie.setMaxAge(60 * 60);
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
            }
        }
        else{
            throw new NoAuthenticationException("No authenticationException");
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/club/boards/{boardId}")
    public ResponseEntity<CommonResult> deleteBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long boardId,
                                                    @RequestBody(required = false) String password, HttpServletResponse response) {

        FreeBoard freeBoard = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("There is no board id '" + boardId + "'"));

        boolean authMember = memberInfo != null; // 인증된 사용자인지
        boolean writtenByAuthMember = freeBoard.getMemberId() != null; // 게시글이 인증된 사용자가 등록한 게시글인지

        if(writtenByAuthMember && authMember){ // 인증된 사용자가 등록한 게시글이면서 로그인 유저인 경우
            boolean sameWriter = freeBoard.getMemberId().equals(memberInfo.getId());
            if(!sameWriter)
                throw new NoAuthorizationException("No authorization for deleting board");
        }
        else if(StringUtils.hasText(password)){  // 인증되지 않은 사용자가 등록한 게시글일 경우
            boolean matchPassword = freeBoard.getPassword().equals(password);
            if (!matchPassword){
                throw new NoAuthenticationException("No authenticationException for deleting board");
            }
        }
        else{
            throw new NoAuthenticationException("No authenticationException for deleting board");
        }

        boardService.deleteBoard(freeBoard);
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/club/boards/{boardId}/like")
    public ResponseEntity<CommonResult> likeBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long boardId) {

        String code = boardService.likeBoard(boardId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, code, null));
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/club/board/comments/{commentId}/auth")
    public ResponseEntity<CommonResult> commentAuth(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        FreeBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("There is no comment with commentId '" + commentId + "'"));

        if (!comment.getMemberId().equals(memberInfo.getId())) {
            throw new NoAuthorizationException("No authorization for modifying comment");
        }

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "AUTH", null));
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/club/boards/{boardId}/comment")
    public ResponseEntity<CommonResult> writeComment(@CurrentMember LoginMember memberInfo, @PathVariable Long boardId,
                                                     @RequestBody String content) {

        if(!StringUtils.hasText(content) || content.length() > MAX_COMMENT_LENGTH) throw new ValidationException("Content is empty or over length 1000");

        CommentSaveDto dto = new CommentSaveDto(
                memberInfo.getId(),
                boardId,
                null,
                memberInfo.getNickname(),
                content
        );

        CommentDto commentDto = boardService.writeComment(dto);

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", commentDto));
    }

    @GetMapping("/club/boards/{boardId}/comments")
    public ResponseEntity<CommonResult> fetchComments(@PathVariable Long boardId,
                                                      @PageableDefault(size = 15, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<FreeBoardComment> page = commentRepository.findPageOfMoreComments(boardId, pageable);
        List<CommentDto> comments = page.stream()
                .map(comment -> new CommentDto(comment))
                .collect(Collectors.toList());

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", comments));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/club/boards/{boardId}/comments/{commentId}/reply")
    public ResponseEntity<CommonResult> writeReply(@CurrentMember LoginMember memberInfo, @PathVariable Long boardId,
                                                   @PathVariable Long commentId, @RequestBody String content) {

        if(!StringUtils.hasText(content) || content.length() > MAX_COMMENT_LENGTH) throw new ValidationException("Content is empty or over length 1000");

        CommentSaveDto dto = new CommentSaveDto(
                memberInfo.getId(),
                boardId,
                commentId,
                memberInfo.getNickname(),
                content
        );

        CommentDto comment = boardService.writeReply(dto);

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", comment));
    }


    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/club/board/comments/{commentId}")
    public ResponseEntity<CommonResult> modifyComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId,
                                                      @RequestBody String content) {

        if(!StringUtils.hasText(content) || content.length() > MAX_COMMENT_LENGTH) throw new ValidationException("Content is empty or over length 1000");

        boardService.updateFreeBoardComment(commentId, memberInfo.getId(), content);

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/club/board/comments/{commentId}")
    public ResponseEntity<CommonResult> deleteComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        DecreaseInfo decreaseInfo = boardService.deleteComment(commentId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", decreaseInfo));
    }


    
    @GetMapping("/club/boards/{boardId}/comments/{commentId}/replies")
    public ResponseEntity<CommonResult> moreReplies(
            @PathVariable Long boardId, @PathVariable Long commentId,
            @PageableDefault(size = 2, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Slice<CommentDto> slice = boardService.getSliceOfMoreComments(boardId, commentId, pageable);

        String code = slice.hasNext() ? "HAS_NEXT" : "NO_NEXT";

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, code, slice.getContent()));
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/club/board/comments/{commentId}/like")
    public ResponseEntity<CommonResult> likeComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        String code = boardService.likeComment(commentId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, code, null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/club/board/comments/{commentId}/unlike")
    public ResponseEntity<CommonResult> unlikeComment(@CurrentMember LoginMember memberInfo, @PathVariable Long commentId) {

        String code = boardService.unlikeComment(commentId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, code, null));
    }


}
