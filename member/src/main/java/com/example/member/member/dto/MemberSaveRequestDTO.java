package com.example.member.member.dto;

import com.example.member.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberSaveRequestDTO {

    private String name;
    private String email;
    private String password;

    public Member toEntity(String encodedPassword) {
        System.out.println("<<< MemberSaveRequestDTO - toEntity() >>>");

        return Member.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .build();
    }
}
