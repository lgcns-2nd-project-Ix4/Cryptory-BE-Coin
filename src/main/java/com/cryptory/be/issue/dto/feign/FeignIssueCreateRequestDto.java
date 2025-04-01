package com.cryptory.be.issue.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

/**
 * packageName    : com.cryptory.be.issue.dto.feign
 * fileName       : IssueCreateRequestDto
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
@ToString
public class FeignIssueCreateRequestDto { //이슈 등록
    private LocalDate date;
    private String title;
    private String content;
    private String newsTitle;
    private String source;
}
