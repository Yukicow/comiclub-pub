package com.comiclub.domain.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

//@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    private String token;

    private LocalDateTime expireTime;

    private Long memberId;


    public RefreshToken(String token, LocalDateTime expireTime, Long memberId) {
        this.token = token;
        this.expireTime = expireTime;
        this.memberId = memberId;
    }

    public RefreshToken(String token) {
        this.token = token;
    }
}
