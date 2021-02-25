package com.tukaloff.tradeterminal.model;

import lombok.Getter;
import lombok.Setter;
import ru.tinkoff.invest.openapi.models.market.Candle;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class Instrument {

    private Portfolio.PortfolioPosition portfolioPosition;
    private List<Candle> candles;
    private Candle lastCandle;

    public Candle getLastCandle() {
        if (lastCandle == null) {
            Optional<Candle> max = candles.stream().max(Comparator.comparing(o -> o.time));
            if (max.isPresent()) {
                lastCandle = max.get();
            }
        }
        return lastCandle;
    }

    public void setLastCandle(Candle lastCandle) {
        this.lastCandle = lastCandle;
    }
}
