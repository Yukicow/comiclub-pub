package com.comiclub.domain.repository.freework;

import com.comiclub.domain.entity.freework.FreeWork;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FreeWorkRepository extends JpaRepository<FreeWork, Long> {



    @Query("SELECT f FROM FreeWork f WHERE f.memberId = :memberId AND f.title LIKE %:title%")
    Slice<FreeWork> searchMyFreeWork(@Param("memberId") Long memberId, @Param("title") String title, Pageable pageable);

    @EntityGraph(attributePaths = {"scenes"})
    Optional<FreeWork> findWithScenesById(@Param("freeWorkId") Long freeWorkId);

    @EntityGraph(attributePaths = {"bgSounds"})
    Optional<FreeWork> findWithBgSoundById(@Param("freeWorkId") Long freeWorkId);



}
