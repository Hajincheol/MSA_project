package com.example.member.member.domain;

import com.example.member.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Member extends BaseTimeEntity {

    // PK 지정, MariaDB의 AUTO_INCREMENT 사용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // @Enumerated => Role이 enum(열거)일 경우 DB에 문자열 데이터로 들어간다.
    // @Builder.Default => @Builder로 객체 생성시 Role.USER를 기본값으로 한다.
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
}
