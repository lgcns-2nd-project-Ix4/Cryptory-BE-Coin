package com.cryptory.be.issue.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * packageName    : com.cryptory.be.issue.dto.feign
 * fileName       : IssueDetailResponseDto
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
public class FeignIssueDetailResponseDto { //이슈 단건
    private Long issueId;
    private LocalDate date;
    private String title;
    private String content;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String newsTitle; //유지
    private String source; //유지
    private String type;
    private Boolean isDeleted;// 추가
}
