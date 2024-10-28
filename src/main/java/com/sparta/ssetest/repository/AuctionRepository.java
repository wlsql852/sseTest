package com.sparta.ssetest.repository;

import com.sparta.ssetest.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
