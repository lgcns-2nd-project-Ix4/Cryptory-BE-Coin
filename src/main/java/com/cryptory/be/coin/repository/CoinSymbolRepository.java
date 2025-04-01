package com.cryptory.be.coin.repository;

import com.cryptory.be.coin.domain.CoinSymbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoinSymbolRepository extends JpaRepository<CoinSymbol, Long> {
    // Code로 CoinSymbol 찾기
    Optional<CoinSymbol> findByCode(String code);
}
