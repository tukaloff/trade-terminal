package com.tukaloff.tradeterminal.controller;

import com.tukaloff.tradeterminal.model.Instrument;
import com.tukaloff.tradeterminal.model.TerminalModel;
import com.tukaloff.tradeterminal.model.TradePosition;
import com.tukaloff.tradeterminal.service.InvestOpenapiService;
import com.tukaloff.tradeterminal.service.StoreTradesService;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tinkoff.invest.openapi.models.market.Candle;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ModelController {

    private TerminalModel terminalModel;

    @Autowired
    private ViewController viewController;

    @Autowired
    private InvestOpenapiService investOpenapiService;

    @Autowired
    private StoreTradesService storeTradesService;

    public ModelController() {
        terminalModel = new TerminalModel();
        terminalModel.setDepth(1);
    }

    @PostConstruct
    private void init() {
        ExecutorService mainLoopExecutorService = Executors.newSingleThreadExecutor();
        terminalModel.setTradePositions(storeTradesService.load());
        mainLoopExecutorService.submit(() -> {
            while (true) {
                update();
                try {
                    Thread.sleep(15_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        investOpenapiService.setSubscriber(new Subscriber<>() {

            Subscription subscription;
            Executor executor = Executors.newFixedThreadPool(10);

            @Override
            public void onSubscribe(Subscription subscription) {
                log.info("onSubscribe {}", subscription);
                if (this.subscription != null)
                    this.subscription.cancel();
                this.subscription = subscription;
                executor.execute(() -> subscription.request(10));
            }

            @Override
            public void onNext(StreamingEvent streamingEvent) {
                log.info("onNext {}", streamingEvent);
                StreamingEvent.Candle streamingEvent1 = (StreamingEvent.Candle) streamingEvent;
                Candle candle = new Candle(streamingEvent1.getFigi(),
                        streamingEvent1.getInterval(),
                        streamingEvent1.getOpenPrice(),
                        streamingEvent1.getClosingPrice(),
                        streamingEvent1.getHighestPrice(),
                        streamingEvent1.getLowestPrice(),
                        streamingEvent1.getTradingValue(),
                        streamingEvent1.getDateTime().toOffsetDateTime());
                getSelected().setLastCandle(candle);
                viewController.updatePlot();
                executor.execute(() -> subscription.request(10));
            }

            @Override
            public void onError(Throwable throwable) {
                log.info("onError {}", throwable);
            }

            @Override
            public void onComplete() {
                log.info("onComplete");
            }
        });
    }

    public void update() {
        List<Portfolio.PortfolioPosition> portfolioPositions = getPortfolioPositions();
        terminalModel.setPortfolioPositions(portfolioPositions);
        viewController.updatePortfolioList(terminalModel.getPortfolioPositions());
        viewController.updateTrades(terminalModel.getTradePositions());
        if (terminalModel.getSelected() != null) {
            List<Candle> candlesLastHour = investOpenapiService
                    .getCandlesLastHour(terminalModel.getSelected().getPortfolioPosition().figi);
            if (candlesLastHour != null) {
                terminalModel.getSelected().setCandles(candlesLastHour);
                viewController.updatePlot();
            }
        }
        viewController.updateCommonView(terminalModel.getSelected());
    }

    public List<Portfolio.PortfolioPosition> getPortfolioPositions() {
        try {
            List<Portfolio.PortfolioPosition> portfolioPositions = investOpenapiService.getPortfolioPositions();
            terminalModel.setPortfolioPositions(portfolioPositions);
            return portfolioPositions;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        return Collections.EMPTY_LIST;
    }

    public Instrument getSelected() {
        return terminalModel.getSelected();
    }

    public void select(Portfolio.PortfolioPosition portfolioPosition) {
        Instrument instrument = new Instrument();
        instrument.setPortfolioPosition(portfolioPosition);
        terminalModel.setSelected(instrument);
        investOpenapiService.subscribe(portfolioPosition.figi);
    }

    public TradePosition createTradePosition(BigDecimal openValue,
                                             BigDecimal openFeePerc,
                                             BigDecimal closeFeePerc,
                                             ZonedDateTime openDate,
                                             String ticker) {
        var tradePosition = new TradePosition();
        tradePosition.setTicker(ticker);
        tradePosition.setOpen(true);
        tradePosition.setOpenDate(openDate.format(DateTimeFormatter.ISO_DATE_TIME));
        tradePosition.setOpenValue(openValue);
        tradePosition.setOpenFeePerc(openFeePerc);
        tradePosition.setOpenFeeValue(openValue.multiply(openFeePerc));
        tradePosition.setCloseFeePerc(closeFeePerc);
        tradePosition.setCloseFeeValue(
                openValue.add(openValue.multiply(openFeePerc)).multiply(closeFeePerc));
        tradePosition.setMinimalSellPrice(openValue
                .add(tradePosition.getOpenFeeValue())
                .add(tradePosition.getCloseFeeValue()));
        tradePosition.setFactSellPrice(new BigDecimal(0));
        tradePosition.setProfit(new BigDecimal(0));
        if (terminalModel.getTradePositions() == null) {
            terminalModel.setTradePositions(new ArrayList<>());
        }
        terminalModel.getTradePositions().add(tradePosition);
        storeTradesService.save(terminalModel.getTradePositions());
        viewController.updateTrades(terminalModel.getTradePositions());
        return tradePosition;
    }

    public BigDecimal getCurrrentPrice(String ticker) {
        Portfolio.PortfolioPosition portfolioPosition = terminalModel.getPortfolioPositions().stream()
                .filter(position -> ticker.equals(position.ticker))
                .findFirst().get();
        try {
            BigDecimal currentPrice =
                    investOpenapiService.getCurrentPrice(portfolioPosition.figi);
            return currentPrice;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(terminalModel.getPortfolioPositions().stream()
                .filter(position -> ticker.equals(position.ticker)).findFirst().get()
                .averagePositionPrice).value;
    }

    public List<TradePosition> getTradePositions() {
        return terminalModel.getTradePositions();
    }

    public void zoomPlot(int value) {
        int depth = terminalModel.getDepth() + value;
        if (depth > terminalModel.getSelected().getCandles().size()/60+1)
            depth = terminalModel.getSelected().getCandles().size()/60+1;
        if (depth < 1) depth = 1;
        terminalModel.setDepth(depth);
    }

    public int getZoom() {
        return terminalModel.getDepth();
    }
}
