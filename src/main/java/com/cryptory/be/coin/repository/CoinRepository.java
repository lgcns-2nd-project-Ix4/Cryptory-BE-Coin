package com.cryptory.be.coin.repository;

import com.cryptory.be.coin.domain.Coin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface CoinRepository extends JpaRepository<Coin, Long> {

    Coin findByCode(String market);

    List<Coin> findByCodeIn(List<String> codes);

    // symbol대신 englishName으로 검색 수정
    // WHERE :keyword IS NULL OR LOWER(c.koreanName) LIKE %:keyword% OR LOWER(c.englishName) LIKE %:keyword%
    @Query("SELECT c FROM Coin c WHERE LOWER(c.koreanName) LIKE :keyword OR LOWER(c.englishName) LIKE :keyword")
    Page<Coin> searchCoins(@Param("keyword") String keyword, Pageable pageable);

    @Transactional
    @Modifying
    @Query("DELETE FROM Coin c WHERE c.id NOT IN :ids")
    void deleteCoinsByIdNotIn(@Param("ids") List<Long> ids);


    @Query("SELECT COUNT(i) FROM Coin i WHERE i.isDisplayed = true")
    long countByIsDisplayedTrue();

    @Transactional
    @Modifying
    @Query("UPDATE Coin c SET c.isDisplayed = true WHERE c.id BETWEEN 1 AND 10")
    void updateCoinDisplaySettings();

}
