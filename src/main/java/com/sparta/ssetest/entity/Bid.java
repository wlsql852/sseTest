package com.sparta.ssetest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="bids")
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auction_id")
    private Auction auction;

    private int price;

    public Bid(Auction auction, int price) {
        this.auction = auction;
        this.price = price;
        this.userId = 1L;
    }
}
