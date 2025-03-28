package com.cryptory.be.issue.controller;

import com.cryptory.be.global.response.ApiResponse;
import com.cryptory.be.issue.dto.IssueDetailDto;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cryptory.be.issue.dto.CreateIssueCommentDto;
import com.cryptory.be.issue.dto.IssueCommentDto;
import com.cryptory.be.issue.dto.UpdateIssueCommentDto;
import com.cryptory.be.issue.service.IssueService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coins/{coinId}/issues")
public class IssueController {
	
	private final IssueService issueService;
	
	// 특정 이슈 상세 조회 - 이슈 기본 정보 (GPT, 토론방 클릭 창)
	@GetMapping("/{issueId}")
	public ApiResponse<IssueDetailDto> getIssueDetail(@PathVariable("coinId") Long coinId,
												  @PathVariable("issueId") Long issueId) {
		IssueDetailDto issueDetail = issueService.getIssueDetail(coinId, issueId);
		return new ApiResponse<>(HttpStatus.OK, issueDetail);
	}

    // 특정 이슈 상세 조회 - 토론방 코멘트 전체 조회
//	@GetMapping("/{issueId}/comments")
//    public ApiResponse<IssueCommentDto> getIssueComments(@PathVariable("coinId") Long coinId,
//														  @PathVariable("issueId") Long issueId) {
//
//		List<IssueCommentDto> issueComments = issueService.getIssueComments(issueId);
//
//		return new ApiResponse<>(HttpStatus.OK, issueComments);
//    }

    // 이슈 내 코멘트 등록
//	@PostMapping("/{issueId}/comments")
//	public ApiResponse<IssueCommentDto> createIssueComment(Principal principal,
//											@PathVariable("coinId") Long coinId,
//											@PathVariable("issueId") Long issueId,
//								            @RequestBody CreateIssueCommentDto createIssueCommentDto) {
//		IssueCommentDto issueComment = issueService.createIssueComment(issueId, principal.getName(), createIssueCommentDto);
//		return new ApiResponse<>(HttpStatus.CREATED, issueComment);
//	}

    // 이슈 내 코멘트 수정
//	@PatchMapping("/{issueId}/comments/{commentId}")
//    public ApiResponse<?> updateIssueComment(@PathVariable("coinId") Long coinId,
//    											@PathVariable("issueId") Long issueId,
//                                              @PathVariable("commentId") Long issueCommentId,
//                                              @RequestBody UpdateIssueCommentDto updateIssueCommentDto) {
//        issueService.updateIssueComment(issueId, issueCommentId, updateIssueCommentDto);
//        return new ApiResponse<>(HttpStatus.OK, "정상적으로 수정되었습니다.");
//    }


    // 이슈 내 코멘트 삭제
//	@PutMapping("/{issueId}/comments/{commentId}")
//    public ApiResponse<?> deleteIssueComment(@PathVariable("coinId") Long coinId,
//    									@PathVariable("issueId") Long issueId,
//                                        @PathVariable("commentId") Long issueCommentId) {
//        issueService.deleteIssueComment(issueId, issueCommentId);
//        return new ApiResponse<>(HttpStatus.OK, "정상적으로 삭제되었습니다.");
//    }

}
