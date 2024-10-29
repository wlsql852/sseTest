package com.sparta.ssetest.entity;

import com.sparta.ssetest.dto.BidRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="bids")
public class Bid extends Timestamped{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auction_id")
    private Auction auction;

    private int price;

    public Bid(BidRequest request, User user, Auction auction) {
        this.price = request.getPrice();
        this.auction = auction;
        this.user = user;
    }
}
