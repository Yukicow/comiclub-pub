package com.comiclub.domain.repository.member;

import com.comiclub.domain.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

    @Modifying
    @Query("UPDATE Member m SET m.viewAdult = :viewAdult WHERE m.id = :id")
    void changeViewAdult(@Param("id") Long id, @Param("viewAdult") boolean viewAdult);
}
