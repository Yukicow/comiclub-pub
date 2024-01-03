package com.comiclub.domain.repository.board.free;

import com.comiclub.domain.entity.board.FreeBoard;
import com.comiclub.domain.entity.board.QFreeBoard;
import com.comiclub.domain.entity.board.QFreeBoardComment;
import com.comiclub.domain.entity.board.enumtype.Topic;
import com.comiclub.web.contoller.club.BoardSearchCond;
import com.comiclub.web.contoller.club.FreeBoardSearchType;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.comiclub.domain.entity.board.QFreeBoard.*;
import static com.comiclub.domain.entity.board.QFreeBoardComment.*;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class JpaFreeBoardRepositoryImpl implements JpaFreeBoardRepository{

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public JpaFreeBoardRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<FreeBoard> search(Long clubId, BoardSearchCond condition, Pageable pageable) {
        List<FreeBoard> content = null;
        if (FreeBoardSearchType.COMMENT.equals(condition.getType())){ // 댓글로 검색 시 join이 필요하기 떄문에 분기
            content = queryFactory
                    .selectFrom(freeBoard)
                    .innerJoin(freeBoardComment, freeBoardComment)
                    .where(
                            freeBoard.clubId.eq(clubId),
                            topicEq(condition),
                            freeBoardComment.content.contains(condition.getKeyword()),
                            adultOnly(condition.getViewAdult())
                    )
                    .orderBy(freeBoard.createdDate.desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        }
        else {
            content = queryFactory
                    .selectFrom(freeBoard)
                    .where(
                            freeBoard.clubId.eq(clubId),
                            topicEq(condition),
                            searchTypeContains(condition),
                            adultOnly(condition.getViewAdult())
                    )
                    .orderBy(freeBoard.createdDate.desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        }

        JPAQuery<Long> countQuery = queryFactory
                .select(freeBoard.count())
                .from(freeBoard)
                .where(
                        freeBoard.clubId.eq(clubId),
                        topicEq(condition),
                        searchTypeContains(condition),
                        adultOnly(condition.getViewAdult())
                );

        /**
         * countQuery의 결과값이 null일 경우 NPE가 발생함. 이유는 new PageImpl<>()의 생성자에서 Long의 value를 호출하는
         * longValue()가 사용되는데 null이면 호출이 불가능하기 때문에 NPE가 발생한다.
         * 문제는 에러 메시지가 안 뜨고 null이라고만 뜨기 때문에 어디서 발생하는 문제인 지 찾기가 쉽지 않음
         */
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public BooleanExpression searchTypeContains(BoardSearchCond condition) {
        FreeBoardSearchType type = condition.getType();
        String keyword = condition.getKeyword();
        switch (type){
            case TITLE:
                return hasText(keyword) ? freeBoard.title.contains(keyword) : null;
            case CONTENT:
                return hasText(keyword) ? freeBoard.content.contains(keyword) : null;
            case WRITER:
                return hasText(keyword) ? freeBoard.writer.contains(keyword) : null;
            default:
                return null;
        }
    }

    public BooleanExpression topicEq(BoardSearchCond condition) {
        Topic topic = condition.getTopic();
        switch (topic){
            case FREE:
                return freeBoard.topic.eq(Topic.FREE);
            case WORK:
                return freeBoard.topic.eq(Topic.WORK);
            case QUESTION:
                return freeBoard.topic.eq(Topic.QUESTION);
            case BEST:
                return freeBoard.totalView.gt(100).and(freeBoard.totalLike.gt(10));
            default:
                return freeBoard.topic.ne(Topic.NOTICE);
        }
    }

    public BooleanExpression adultOnly(Boolean viewAdult) {
            return viewAdult
                    ? null
                    : freeBoard.adultOnly.isFalse();
    }

    @Override
    public List<FreeBoard> searchNotice(Long clubId, boolean viewAdult) {
        return queryFactory
                .selectFrom(freeBoard)
                .where(
                        freeBoard.clubId.eq(clubId),
                        freeBoard.topic.eq(Topic.NOTICE),
                        adultOnly(viewAdult)
                )
                .fetch();
    }


}
