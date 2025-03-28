package com.cryptory.be.issue.service;

import com.cryptory.be.coin.domain.Coin;
import com.cryptory.be.coin.exception.CoinErrorCode;
import com.cryptory.be.coin.exception.CoinException;
import com.cryptory.be.coin.repository.CoinRepository;
import com.cryptory.be.issue.domain.Issue;
import com.cryptory.be.issue.dto.IssueDetailDto;
import com.cryptory.be.issue.exception.IssueErrorCode;
import com.cryptory.be.issue.exception.IssueException;
import com.cryptory.be.issue.repository.IssueRepository;
import com.cryptory.be.openapi.dto.Ticker;
import com.cryptory.be.openapi.service.UpbitService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cryptory.be.global.util.DateFormat;
import com.cryptory.be.issue.domain.IssueComment;
import com.cryptory.be.issue.dto.CreateIssueCommentDto;
import com.cryptory.be.issue.dto.IssueCommentDto;
import com.cryptory.be.issue.dto.UpdateIssueCommentDto;
import com.cryptory.be.issue.repository.IssueCommentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueService {

    private final IssueRepository issueRepository;
    //private final IssueCommentRepository issueCommentRepository;

    private final int END_OF_KRW = 4;

//    // 특정 이슈 상세 조회 - 토론방 코멘트 전체 조회
//    public List<IssueCommentDto> getIssueComments(Long issueId) {
//
//        return issueCommentRepository.findAllByIssueId(issueId).stream()
//                .filter(IssueComment::isNotDeleted)
//                .map(issueComment -> new IssueCommentDto(
//                        issueComment.getId(),
//                        issueComment.getContent(),
//                        issueComment.getUser().getNickname(),
//                        DateFormat.formatDate(issueComment.getCreatedAt())
//                ))
//                .toList();
//
//    }

    // 이슈 내 코멘트 등록
//    @Transactional
//    public IssueCommentDto createIssueComment(Long issueId, String userId, CreateIssueCommentDto createIssueCommentDto) {
//
//        //User user = userRepository.findByUserId(userId)
//
//        Issue issue = issueRepository.findById(issueId)
//                .orElseThrow(() -> new IssueException(IssueErrorCode.NOT_EXIST_ISSUE));
//
//        IssueComment issueComment = issueCommentRepository.save(IssueComment.builder()
//                .content(createIssueCommentDto.getContent())
//                .userId(userId)
//                .issue(issue)
//                .build());
//
//        return new IssueCommentDto(
//                issueComment.getId(),
//                issueComment.getContent(),
//                issueComment.getUser().getNickname(),
//                DateFormat.formatDate(issueComment.getCreatedAt())
//        );
//    }

    // 이슈 내 코멘트 수정
//    @Transactional
//    public void updateIssueComment(Long issueId, Long issueCommentId, UpdateIssueCommentDto updateIssueCommentDto) {
//        IssueComment issueComment = issueCommentRepository.findById(issueCommentId)
//                .orElseThrow(() -> new IssueException(IssueErrorCode.NOT_EXIST_ISSUE_COMMENT));
//
//        issueComment.update(updateIssueCommentDto.getContent());
//    }
//
//    // 이슈 내 코멘트 삭제
//    @Transactional
//    public void deleteIssueComment(Long issueId, Long issueCommentId) {
//        IssueComment issueComment = issueCommentRepository.findById(issueCommentId)
//                .orElseThrow(() -> new IssueException(IssueErrorCode.NOT_EXIST_ISSUE_COMMENT));
//
//        issueComment.delete();
//    }

    public IssueDetailDto getIssueDetail(Long coinId, Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IssueException(IssueErrorCode.NOT_EXIST_ISSUE));

        if(issue.isDeleted()) {
            throw new IssueException(IssueErrorCode.NOT_EXIST_ISSUE);
        }

        return new IssueDetailDto(issue.getTitle(), issue.getContent(), issue.getNewsTitle(), issue.getSource());
    }
}
