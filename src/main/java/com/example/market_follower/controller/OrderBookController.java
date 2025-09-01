package com.example.market_follower.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/orderbook")
@RequiredArgsConstructor
@Tag(name = "OrderBook API", description = "암호화폐 호가 API")
public class OrderBookController {

}
