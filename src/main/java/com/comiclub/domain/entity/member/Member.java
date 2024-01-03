package com.comiclub.domain.entity.member;

import com.comiclub.domain.entity.BaseDateEntity;
import com.comiclub.web.contoller.common.form.JoinForm;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseDateEntity {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 15)
    private String nickname;

    @Column(nullable = false)
    private Boolean adult;

    @Column(nullable = false)
    private Boolean viewAdult;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private Role role;

    public Member(Long id) {
        this.id = id;
    }

    private Member(String loginId, String password, String nickname, Role role) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.adult = false;
        this.viewAdult = false;
        this.role = role;
    }

    public static Member createNewMember(JoinForm form, PasswordEncoder encoder, Role role) {
        return new Member(
                form.getLoginId(),
                encoder.encode(form.getPassword()),
                form.getNickname(),
                role
        );
    }

}
