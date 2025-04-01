package com.cryptory.be.issue.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * packageName    : com.cryptory.be.issue.dto.feign
 * fileName       : IssueUpdateRequestDto
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
public class FeignIssueUpdateRequestDto {
    private String title;
    private String content;
    private String newsTitle;
    private  String source;
}
