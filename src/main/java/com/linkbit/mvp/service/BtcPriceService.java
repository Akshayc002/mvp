package com.linkbit.mvp.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class BtcPriceService {

    private static final String BINANCE_PRICE_URL =
            "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";

    private final RestTemplate restTemplate;
    private final AtomicReference<BigDecimal> cachedBtcPrice =
            new AtomicReference<>(new BigDecimal("73000.00"));

    public BtcPriceService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Scheduled(fixedRate = 1000)
    public void fetchLatestBtcPrice() {
        try {
            JsonNode response = restTemplate.getForObject(BINANCE_PRICE_URL, JsonNode.class);
            if (response != null && response.has("price")) {
                BigDecimal latestBtcUsdt = new BigDecimal(response.get("price").asText())
                        .setScale(2, RoundingMode.HALF_UP);
                cachedBtcPrice.set(latestBtcUsdt);
                log.debug("BTC/USDT price updated from Binance: {}", latestBtcUsdt);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch BTC price from Binance: {}", e.getMessage());
        }
    }

    public BigDecimal getCurrentBtcPrice() {
        return cachedBtcPrice.get();
    }
}
