package com.sparta.ssetest.controller;

import com.sparta.ssetest.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BidController {

    private final BidService bidService;

}
