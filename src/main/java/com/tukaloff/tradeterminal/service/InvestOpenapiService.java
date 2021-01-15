package com.tukaloff.tradeterminal.service;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.models.market.Candle;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.streaming.StreamingRequest;
import ru.tinkoff.invest.openapi.models.user.BrokerAccount;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Slf4j
@Service
public class InvestOpenapiService {

    @Value("${tinkoff.openapi.token}")
    private String token;

    private OpenApi api;

    private BrokerAccount brokerAccount;

    @PostConstruct
    private void init() {
        log.info(token);
        Logger logger = Logger.getLogger(this.getClass().getName());
        OkHttpOpenApiFactory factory = new OkHttpOpenApiFactory(token, logger);
        api = factory.createOpenApiClient(Executors.newSingleThreadExecutor());
        try {
            brokerAccount = api.getUserContext().getAccounts().get()
                    .accounts.stream().findFirst().get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Portfolio.PortfolioPosition> getPortfolioPositions() throws ExecutionException, InterruptedException {
        List<Portfolio.PortfolioPosition> positions = api.getPortfolioContext()
                .getPortfolio(brokerAccount.brokerAccountId).get().positions;
        return positions;
    }

    private void test() {
        Subscriber subscriber = new Subscriber() {
            @Override
            public void onSubscribe(Subscription subscription) {
                log.info("onSubscribe", subscription);
            }

            @Override
            public void onNext(Object o) {
                log.info("onNext", o);
            }

            @Override
            public void onError(Throwable throwable) {
                log.info("onError", throwable);

            }

            @Override
            public void onComplete() {
                log.info("onComplete");

            }
        };
        api.getStreamingContext().getEventPublisher().subscribe(subscriber);
        Instrument tesla = null;
        try {
            tesla = api.getMarketContext().searchMarketInstrumentsByTicker("TSLA").get()
                    .instruments.stream().findFirst().get();
            log.info(tesla.toString());

            api.getStreamingContext().sendRequest(StreamingRequest.subscribeCandle(tesla.figi, CandleInterval.ONE_MIN));
            BrokerAccount brokerAccount = api.getUserContext().getAccounts().get().accounts.stream().findFirst().get();
            api.getPortfolioContext().getPortfolio(brokerAccount.brokerAccountId).get().positions
                    .forEach(portfolioPosition -> System.out.println(portfolioPosition.toString()));
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public Portfolio.PortfolioPosition getPortfolioPosition(String figi) throws ExecutionException, InterruptedException {
        return api.getPortfolioContext().getPortfolio(brokerAccount.brokerAccountId).get()
                .positions.stream().filter(portfolioPosition -> figi.equals(portfolioPosition.figi)).findFirst().get();
    }

    public BigDecimal getCurrentPrice(String figi) throws ExecutionException, InterruptedException {
        ZonedDateTime from = ZonedDateTime.now().minusMinutes(2);
        ZonedDateTime to = ZonedDateTime.now();
        log.info("{} {}", from.toOffsetDateTime(), to.toOffsetDateTime());
        List<Candle> closePrice = api.getMarketContext().getMarketCandles(figi,
                from.toOffsetDateTime(),
                to.toOffsetDateTime(),
                CandleInterval.ONE_MIN).get().get().candles;
        log.info(closePrice.stream().findFirst().get().closePrice.toEngineeringString());
        return closePrice.stream().findFirst().get().closePrice;
    }

    public List<Candle> getCandlesLastHour(String figi) {
        ZonedDateTime from = ZonedDateTime.now().minusHours(24);
        ZonedDateTime to = ZonedDateTime.now();
        log.info("{} {}", from.toOffsetDateTime(), to.toOffsetDateTime());
        try {
            Optional<HistoricalCandles> historicalCandles = api.getMarketContext().getMarketCandles(figi,
                    from.toOffsetDateTime(),
                    to.toOffsetDateTime(),
                    CandleInterval.ONE_MIN).get();
            if (historicalCandles.isPresent())
                return historicalCandles.get().candles;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
