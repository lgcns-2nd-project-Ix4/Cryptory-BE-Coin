package com.cryptory.be.issue.service;

import com.cryptory.be.chart.domain.Chart;
import com.cryptory.be.chart.repository.ChartRepository;
import com.cryptory.be.coin.domain.Coin;
import com.cryptory.be.coin.repository.CoinRepository;
import com.cryptory.be.issue.domain.Issue;
import com.cryptory.be.issue.dto.IssueDetailDto;
import com.cryptory.be.issue.dto.feign.FeignIssueCreateRequestDto;
import com.cryptory.be.issue.dto.feign.FeignIssueDetailResponseDto;
import com.cryptory.be.issue.dto.feign.FeignIssueListResponseDto;
import com.cryptory.be.issue.dto.feign.FeignIssueUpdateRequestDto;
import com.cryptory.be.issue.exception.IssueErrorCode;
import com.cryptory.be.issue.exception.IssueException;
import com.cryptory.be.issue.repository.IssueRepository;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueService { // 인터페이스 없이 바로 클래스 구현

    private final IssueRepository issueRepository;
    private final CoinRepository coinRepository;
    private final ChartRepository chartRepository;

    /**
     * 관리자: 특정 코인의 이슈 목록 조회 (페이징)
     */
    public Page<FeignIssueListResponseDto> getIssueListForAdmin(Long coinId, int page, int size) {
        // 삭제된 이슈도 포함해서 보여줄지 여부 등 관리자 정책에 따라 쿼리 조정 가능
        // 여기서는 삭제되지 않은 것만 조회
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Issue> issuesPage = issueRepository.findByCoinIdAndIsDeletedFalse(coinId, pageable);

        List<FeignIssueListResponseDto> dtoList = issuesPage.getContent().stream()
                .map(this::convertToIssueListResponseDto) // 헬퍼 메서드 사용
                .toList();

        return new PageImpl<>(dtoList, pageable, issuesPage.getTotalElements());
    }

    /**
     * 관리자: 특정 코인 이슈 생성
     */
    @Transactional
    public Long createIssue(Long coinId, FeignIssueCreateRequestDto requestDto, String adminUserIdString) {
        Coin coin = coinRepository.findById(coinId)
                .orElseThrow(() -> new NoSuchElementException("코인 정보 없음 ID: " + coinId));

        // 날짜와 코인 ID로 차트 조회 (차트가 필수인지, 없어도 되는지 정책 확인 필요)
        // 날짜 포맷팅 주의: requestDto.getDate()가 LocalDate 라면
        String dateStr = requestDto.getDate().format(DateTimeFormatter.ISO_DATE); // "yyyy-MM-dd"
        Optional<Chart> chartOpt = chartRepository.findByDateAndCoinId(dateStr, coinId); // 정확한 날짜 조회
        // Chart chart = chartOpt.orElse(null); // 차트가 없으면 null 할당

        // User ID 처리 (Long 타입 가정 - 실제 User ID 타입에 맞춰야 함)
        Long adminUserId;
        try {
            // user-service에서 전달된 ID가 Long으로 변환 가능한지 확인
            adminUserId = Long.parseLong(adminUserIdString);
        } catch (NumberFormatException e) {
            log.error("전달받은 관리자 ID '{}'를 Long으로 변환할 수 없습니다.", adminUserIdString);
            // 또는 UUID String 그대로 저장한다면 타입 변경
            throw new IllegalArgumentException("잘못된 관리자 ID 형식입니다.");
        }

        Issue newIssue = Issue.builder()
                .date(requestDto.getDate())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .newsTitle(requestDto.getNewsTitle())
                .source(requestDto.getSource())
                .type("MANUAL") // 관리자 생성
                .userId(adminUserId) // 변환된 ID 사용
                .coin(coin)
                .chart(chartOpt.orElse(null)) // 차트가 없으면 null
                .requestCount(0L)
                .isDeleted(false)
                .build();

        Issue savedIssue = issueRepository.save(newIssue);
        log.info("관리자 이슈 생성됨 (ID: {}) by Admin ID: {}", savedIssue.getId(), adminUserId);
        return savedIssue.getId();
    }

    /**
     * 관리자: 이슈 상세 조회
     */
    public FeignIssueDetailResponseDto getIssueDetailsForAdmin(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                // 관리자는 삭제된 이슈도 볼 수 있게 할지? -> 여기서는 isDeleted=false 조건 제거
                .orElseThrow(() -> new NoSuchElementException("해당 이슈를 찾을 수 없습니다. ID: " + issueId));
        // 헬퍼 메서드 사용
        return convertToIssueDetailResponseDto(issue);
    }

    /**
     * 관리자: 이슈 수정
     */
    @Transactional
    public void updateIssue(Long issueId, FeignIssueUpdateRequestDto requestDto) {
        Issue issue = issueRepository.findById(issueId)
                // 관리자는 삭제된 이슈도 수정 가능? -> 여기서는 isDeleted=false 조건 제거
                .orElseThrow(() -> new NoSuchElementException("수정할 이슈를 찾을 수 없습니다. ID: " + issueId));
        issue.update(requestDto.getTitle(), requestDto.getContent(), requestDto.getNewsTitle(), requestDto.getSource());
        log.info("관리자 이슈 수정됨 (ID: {})", issueId);
    }

    /**
     * 관리자: 이슈 삭제 (논리적 삭제)
     */
    @Transactional
    public void deleteIssues(List<Long> ids) {
        List<Issue> issues = issueRepository.findAllById(ids);
        if (issues.size() != ids.size()) {
            log.warn("삭제 요청된 이슈 ID 중 일부가 존재하지 않을 수 있습니다. 요청 ID: {}, 찾은 개수: {}", ids, issues.size());
            // 필요시 여기서 예외 발생
        }
        issues.forEach(Issue::delete);
        issueRepository.saveAll(issues); // 변경 감지 또는 명시적 저장
        log.info("관리자 이슈 논리적 삭제 완료 (IDs: {})", ids);
    }


    // --- 일반 사용자용 이슈 상세 조회 (기존 코드) ---
    public IssueDetailDto getIssueDetail(Long coinId, Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> !i.isDeleted()) // 일반 사용자는 삭제되지 않은 것만 조회
                .orElseThrow(() -> new IssueException(IssueErrorCode.NOT_EXIST_ISSUE));
        // 일반 사용자에게 필요한 정보만 담은 DTO 반환
        return new IssueDetailDto(issue.getTitle(), issue.getContent(), issue.getNewsTitle(), issue.getSource());
    }


    // --- Private 헬퍼 메서드 (DTO 변환) ---
    private FeignIssueListResponseDto convertToIssueListResponseDto(Issue issue) {
        // com.cryptory.be.issue.dto.IssueListResponseDto 사용
        return FeignIssueListResponseDto.builder()
                .issueId(issue.getId())
                .date(issue.getDate())
                .title(issue.getTitle())
                .createdBy(issue.getUserId() != null ? String.valueOf(issue.getUserId()) : "Unknown") // UserId 사용
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }

    private FeignIssueDetailResponseDto convertToIssueDetailResponseDto(Issue issue) {
        // com.cryptory.be.issue.dto.IssueDetailResponseDto 사용
        return FeignIssueDetailResponseDto.builder()
                .issueId(issue.getId())
                .date(issue.getDate())
                .title(issue.getTitle())
                .content(issue.getContent())
                .createdBy(issue.getUserId() != null ? String.valueOf(issue.getUserId()) : "Unknown") // UserId 사용
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .newsTitle(issue.getNewsTitle())
                .source(issue.getSource())
                .type(issue.getType())
                .isDeleted(issue.isDeleted()) // 관리자는 삭제 여부 확인 가능
                .build();
    }

}


