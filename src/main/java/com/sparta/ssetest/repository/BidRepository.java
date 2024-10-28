package com.sparta.ssetest.repository;

import com.sparta.ssetest.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
}
