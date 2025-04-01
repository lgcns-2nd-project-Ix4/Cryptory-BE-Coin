package com.cryptory.be.coin.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coin_symbols", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
@EqualsAndHashCode(of = "id", callSuper = false)
public class CoinSymbol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String color;

    @Column(columnDefinition = "TEXT")
    private String logoUrl;

    @Builder
    public CoinSymbol(String code, String color, String logoUrl) {
        this.code = code;
        this.color = color;
        this.logoUrl = logoUrl;
    }
}
