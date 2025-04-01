package com.cryptory.be.issue.controller;

import com.cryptory.be.global.response.ApiResponse;
import com.cryptory.be.issue.dto.IssueDetailDto;
import com.cryptory.be.issue.dto.feign.FeignIssueDetailResponseDto;
import com.cryptory.be.issue.dto.feign.FeignIssueCreateRequestDto;
import com.cryptory.be.issue.dto.feign.FeignIssueListResponseDto;
import com.cryptory.be.issue.dto.feign.FeignIssueUpdateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.cryptory.be.issue.service.IssueService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Slf4j
@RestController
@RequestMapping("/api/v1") // 기본 경로
@RequiredArgsConstructor
public class IssueController {

	private final IssueService issueService;

	// --- 일반 사용자용 엔드포인트 ---
	@GetMapping("/coins/{coinId}/issues/{issueId}")
	public ApiResponse<IssueDetailDto> getPublicIssueDetail(@PathVariable("coinId") Long coinId,
															@PathVariable("issueId") Long issueId) {
		IssueDetailDto issueDetail = issueService.getIssueDetail(coinId, issueId);
		return new ApiResponse<>(HttpStatus.OK, issueDetail);
	}

	// --- 관리자용 엔드포인트 ---

	@GetMapping("/admin/coins/{coinId}/issues")
	public ResponseEntity<Page<FeignIssueListResponseDto>> getAdminIssueList(
			@PathVariable Long coinId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		try {
			Page<FeignIssueListResponseDto> issuesPage = issueService.getIssueListForAdmin(coinId, page, size);
			return ResponseEntity.ok(issuesPage);
		} catch (Exception e) {
			log.error("관리자 이슈 목록 조회 오류 (coinId: {}): {}", coinId, e.getMessage(), e);
			// 실제로는 ErrorResponse DTO를 반환하는 것이 더 좋음
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/admin/coins/{coinId}/issues")
	public ResponseEntity<Void> createAdminIssue(
			@PathVariable Long coinId,
			@Valid @RequestBody FeignIssueCreateRequestDto requestDto,
			@RequestHeader("X-Admin-User-Id") String adminUserId) { // 헤더로 ID 받기
		try {
			Long newIssueId = issueService.createIssue(coinId, requestDto, adminUserId);
			URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/api/v1/admin/issues/{issueId}") // 관리자 조회 경로
					.buildAndExpand(newIssueId)
					.toUri();
			return ResponseEntity.created(location).build();
		} catch (NoSuchElementException | IllegalArgumentException e) {
			log.warn("관리자 이슈 생성 실패: {}", e.getMessage());
			// 클라이언트 오류 상태 코드와 메시지 반환 고려
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			log.error("관리자 이슈 생성 오류: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/admin/issues/{issueId}")
	public ResponseEntity<FeignIssueDetailResponseDto> getAdminIssueDetails(@PathVariable Long issueId) {
		try {
			FeignIssueDetailResponseDto issue = issueService.getIssueDetailsForAdmin(issueId);
			return ResponseEntity.ok(issue);
		} catch (NoSuchElementException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			log.error("관리자 이슈 상세 조회 오류 (issueId: {}): {}", issueId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PutMapping("/admin/issues/{issueId}") // 전체 수정을 가정하여 PUT 사용 (부분 수정 시 PATCH)
	public ResponseEntity<Void> updateAdminIssue(@PathVariable Long issueId, @Valid @RequestBody FeignIssueUpdateRequestDto requestDto) {
		try {
			issueService.updateIssue(issueId, requestDto);
			return ResponseEntity.ok().build();
		} catch (NoSuchElementException e) {
			return ResponseEntity.notFound().build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build(); // 에러 메시지 본문에 추가 가능
		} catch (Exception e) {
			log.error("관리자 이슈 수정 오류 (issueId: {}): {}", issueId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/admin/issues")
	public ResponseEntity<Void> deleteAdminIssues(@RequestParam("ids") List<Long> ids) {
		try {
			if (ids == null || ids.isEmpty()) {
				return ResponseEntity.badRequest().build();
			}
			issueService.deleteIssues(ids);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			log.error("관리자 이슈 삭제 오류 (IDs: {}): {}", ids, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}