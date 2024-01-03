package com.comiclub.domain.repository.series;

import com.comiclub.domain.entity.histroy.FollowHistory;
import com.comiclub.domain.entity.histroy.QFollowHistory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.comiclub.domain.entity.histroy.QFollowHistory.*;

public class JpaFollowHistoryRepositoryImpl implements JpaFollowHistoryRepository{


    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public JpaFollowHistoryRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public Page<FollowHistory> findFollowList(Long memberId, boolean viewAdult, Pageable pageable) {
        List<FollowHistory> histories = queryFactory
                .selectFrom(followHistory)
                .where(
                        followHistory.memberId.eq(memberId),
                        adultOnly(viewAdult)
                )
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(followHistory.count())
                .from(followHistory)
                .where(
                        followHistory.memberId.eq(memberId),
                        adultOnly(viewAdult)
                );

        return PageableExecutionUtils.getPage(histories, pageable, () -> countQuery.fetchOne());
    }

    private BooleanExpression adultOnly(boolean viewAdult){
        return viewAdult ? null : followHistory.adultOnly.isFalse();
    }

}
