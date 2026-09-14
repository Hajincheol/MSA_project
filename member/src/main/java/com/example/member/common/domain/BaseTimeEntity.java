package com.example.member.common.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 해당 클래스를 Entity의 부모 클래스로 사용하고, 이 클래스의 필드를 자식 Entity의 컬럼으로 매핑
@MappedSuperclass
@Getter
public class BaseTimeEntity {

    // 년원일시분초까지 나옴
    // 생성 시간이 자동 업데이트
    @CreationTimestamp
    private LocalDateTime createdTime;

    // 최종 수정 시간이 자동 업데이트
    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
