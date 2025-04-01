package com.cryptory.be.coin.domain;

import com.cryptory.be.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coins")
@EqualsAndHashCode( of = "id", callSuper = false)
public class Coin extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String koreanName;

    private String englishName;

    private boolean isDisplayed;

    private String code;    // 마켓 코드(KRW-BTC, KRW-ETH, KRW-XRP)

    public void setIsDisplayed(boolean isDisplayed){
        this.isDisplayed = isDisplayed;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_symbol_id")
    private CoinSymbol coinSymbol;  // 코인 심볼 관련 정보

    @Builder
    public Coin(String koreanName, String englishName, String code, CoinSymbol coinSymbol, boolean isDisplayed){
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.code = code;
        this.coinSymbol = coinSymbol;
        this.isDisplayed = isDisplayed;
    }
}