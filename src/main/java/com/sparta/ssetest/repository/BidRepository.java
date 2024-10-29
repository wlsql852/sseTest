package com.sparta.ssetest.repository;

import com.sparta.ssetest.entity.Auction;
import com.sparta.ssetest.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findAllByAuctionOrderByCreatedAtDesc(Auction auction);
}
