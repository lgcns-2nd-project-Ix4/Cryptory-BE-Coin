package com.cryptory.be.issue.domain;

import com.cryptory.be.chart.domain.Chart;
import com.cryptory.be.coin.domain.Coin;
import com.cryptory.be.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "issues")
public class Issue extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 일자
    private LocalDate date;

    // 이슈 제목
    private String title;

    // 이슈 요약 내용
    @Column(columnDefinition = "TEXT")
    private String content;

    // 뉴스 제목
    private String newsTitle;

    // 뉴스 출처
    private String source;

    // 이슈 생성 타입
    private String type;

    // AI 생성 요청 횟수
    private Long requestCount;

    // 삭제 여부
    private boolean isDeleted = false;

    private Long userId;

    // 하나의 차트에 대해 이슈 한 개만 등록
    @OneToOne(fetch = FetchType.EAGER)  // 즉시 로딩
    @JoinColumn(name = "chart_id", unique = true, nullable = true)
    private Chart chart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @Builder // 모든 필드를 초기화 하는 생성자
    public Issue(String title, String content, LocalDate date, String newsTitle,
                 String source, String type, Long requestCount,
                 boolean isDeleted, Coin coin, Long userId, Chart chart) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.newsTitle = newsTitle;
        this.source = source;
        this.type = type;
        this.requestCount = requestCount;
        this.isDeleted = isDeleted;
        this.coin = coin;
        this.userId = userId;
        this.chart = chart;
    }

    public void update(String title, String content, String newsTitle, String source) {
        if (title != null && title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어 있을 수 없습니다.");
        }
        if (content != null && content.isBlank()) {
            throw new IllegalArgumentException("내용은 비어 있을 수 없습니다.");
        }

        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (newsTitle != null) {
            this.newsTitle = newsTitle;
        }
        if (source != null) {
            this.source = source;
        }
    }

    public void delete() {
        this.isDeleted = true;
    }
}
