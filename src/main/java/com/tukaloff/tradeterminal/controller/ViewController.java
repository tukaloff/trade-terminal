package com.tukaloff.tradeterminal.controller;

import com.tukaloff.tradeterminal.model.Instrument;
import com.tukaloff.tradeterminal.model.TradePosition;
import com.tukaloff.tradeterminal.view.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ViewController {

    @Autowired
    private ModelController modelController;

    @Autowired
    private MainForm mainForm;

    private Map<Component, ChangeListener> changeListeners;

    private ExecutorService executorService;
    private List<SelectionListener> selectionListeners;

    public ViewController() {
        this.changeListeners = new HashMap<>();
        this.selectionListeners = new ArrayList<>();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public List<Portfolio.PortfolioPosition> getPortfolioPositions() {
        try {
            return modelController.getPortfolioPositions();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        return Collections.EMPTY_LIST;
    }

    public Instrument getSelected() {
        return modelController.getSelected();
    }

    public void select(PortfolioPositionItem component) {
        log.info("selected: {}", component.getPosition());
        executorService.submit(() -> {
            modelController.select(component.getPosition());
            modelController.update();
            selectionListeners.forEach(SelectionListener::onSelect);
        });
    }

    public void updatePortfolioList(List<Portfolio.PortfolioPosition> portfolioPositions) {
        executorService.submit(() ->
                changeListeners.get(Component.PORTFOLIO_POSITION_LIST_LISTENER).redraw(portfolioPositions));
    }

    public void addListener(ChangeListener changeListener, Component type) {
        changeListeners.put(type, changeListener);
    }

    public void createTradePosition(String openValue,
                                    String openFeePerc,
                                    String closeFeePerc,
                                    String ticker) {
        BigDecimal ov = new BigDecimal(openValue);
        BigDecimal ofp = new BigDecimal(openFeePerc);
        BigDecimal cfp = new BigDecimal(closeFeePerc);
        ZonedDateTime opDate = ZonedDateTime.now();
        modelController.createTradePosition(ov, ofp, cfp, opDate, ticker);
    }

    public void updateTrades(List<TradePosition> tradePositions) {
        executorService.submit(() ->
                changeListeners.get(Component.TRADE_POSITIONS_LISTENER).redraw(tradePositions));
    }

    public BigDecimal getCurrentPrice(String ticker) {
        return modelController.getCurrrentPrice(ticker);
    }

    public void updateCommonView(Instrument selected) {
        executorService.submit(() ->
                changeListeners.get(Component.COMMON_VIEW_LISTENER).redraw(selected));
    }

    public void addSelectionListener(SelectionListener listener) {
        this.selectionListeners.add(listener);
    }
}
