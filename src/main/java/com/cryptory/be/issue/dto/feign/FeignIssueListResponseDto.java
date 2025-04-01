package com.cryptory.be.issue.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * packageName    : com.cryptory.be.issue.dto.feign
 * fileName       : IssueListResponseDto
 * author         : 조영상
 * date           : 4/1/25
 * description    : 자동 주석 생성
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 4/1/25         조영상        최초 생성
 */
@Getter
@Builder
@AllArgsConstructor
@Jacksonized
public class FeignIssueListResponseDto { //이슈 목록
    private Long issueId;
    private LocalDate date;
    private String title;
    private String createdBy; // 또는 String createdBy (닉네임)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
