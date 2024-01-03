package com.comiclub.domain.repository.board.freework;

import com.comiclub.domain.entity.board.FreeWorkBoard;
import com.comiclub.web.contoller.board.OrderType;
import com.comiclub.web.contoller.board.freework.dto.FreeWorkBoardSearchCond;
import com.comiclub.web.contoller.board.freework.FreeWorkSearchType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.comiclub.domain.entity.board.QFreeWorkBoard.*;


@Repository
public class JpaFWBoardRepositoryImpl implements JpaFWBoardRepository {


    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public JpaFWBoardRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<FreeWorkBoard> searchFreeWorkBoard(FreeWorkBoardSearchCond cond, Pageable pageable){
        List<FreeWorkBoard> freeWorkBoards = queryFactory
                .selectFrom(freeWorkBoard)
                .where(
                        searchCondition(cond),
                        freeWorkBoard.pub.isTrue(),
                        cond.getViewAdult() ? null : freeWorkBoard.adultOnly.isFalse()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(freeWorkBoardOrder(cond))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(freeWorkBoard.count())
                .from(freeWorkBoard)
                .where(
                        searchCondition(cond),
                        freeWorkBoard.pub.isTrue(),
                        cond.getViewAdult() ? null : freeWorkBoard.adultOnly.isFalse()
                );

        return PageableExecutionUtils.getPage(freeWorkBoards, pageable, countQuery::fetchOne);
    };

    private BooleanExpression searchCondition(FreeWorkBoardSearchCond cond) {
        String keyword = cond.getKeyword();
        if(!StringUtils.hasText(keyword)) return null;

        FreeWorkSearchType type = cond.getType();
        switch (type){
            case TITLE:
                return freeWorkBoard.title.contains(keyword);
            case WRITER:
                return freeWorkBoard.writer.contains(keyword);
            default:
                return null;
        }
    }

    private OrderSpecifier freeWorkBoardOrder(FreeWorkBoardSearchCond cond) {
        OrderType order = cond.getOrder();
        switch (order){
            case BEST:
                return freeWorkBoard.totalLike.desc();
            default:
                return freeWorkBoard.createdDate.desc();
        }
    }


    @Override
    public List<FreeWorkBoard> findWritersFreeWorkBoards(Long memberId, boolean adultOnly, Pageable pageable) {
        return queryFactory
                .selectFrom(freeWorkBoard)
                .where(
                        freeWorkBoard.memberId.eq(memberId),
                        adultOnly ? null : freeWorkBoard.adultOnly.eq(false),
                        freeWorkBoard.pub.isTrue()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(freeWorkBoard.totalLike.desc())
                .fetch();
    }

    @Override
    public Optional<FreeWorkBoard> findOneByWorkId(Long workId) {
        return Optional.ofNullable(
                queryFactory
                .selectFrom(freeWorkBoard)
                .where(freeWorkBoard.freeWorkId.eq(workId))
                .fetchFirst()
        );
    }

    @Override
    public List<FreeWorkBoard> findDayBestBoards() {
        return queryFactory
                .selectFrom(freeWorkBoard)
                .where(
                        freeWorkBoard.createdDate.after(LocalDateTime.now().minusDays(1)),
                        freeWorkBoard.pub.isTrue()
                )
                .orderBy(freeWorkBoard.totalLike.desc())
                .limit(10)
                .fetch();
    }

    @Override
    public List<FreeWorkBoard> findWeekBestBoards() {
        return queryFactory
                .selectFrom(freeWorkBoard)
                .where(
                        freeWorkBoard.createdDate.after(LocalDateTime.now().minusDays(1)),
                        freeWorkBoard.pub.isTrue()
                )
                .orderBy(freeWorkBoard.totalLike.desc())
                .limit(10)
                .fetch();
    }


    @Override
    public List<FreeWorkBoard> findRankBoards() {
        return queryFactory
                .selectFrom(freeWorkBoard)
                .where(
                        freeWorkBoard.pub.isTrue()
                )
                .orderBy(freeWorkBoard.totalLike.desc())
                .limit(10)
                .fetch();
    }



    @Override
    public Page<FreeWorkBoard> adminSearchFreeWorkBoard(FreeWorkBoardSearchCond cond, Pageable pageable){
        List<FreeWorkBoard> freeWorkBoards = queryFactory
                .selectFrom(freeWorkBoard)
                .where(searchCondition(cond))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(freeWorkBoardOrder(cond))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(freeWorkBoard.count())
                .from(freeWorkBoard)
                .where(searchCondition(cond));

        return PageableExecutionUtils.getPage(freeWorkBoards, pageable, countQuery::fetchOne);
    };
}
