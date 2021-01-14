package com.tukaloff.tradeterminal.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ViewController {

    @Autowired
    private ModelController modelController;

    public List<Portfolio.PortfolioPosition> getPortfolioPositions() {
        try {
            return modelController.getPortfolioPositions();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        return Collections.EMPTY_LIST;
    }

    public Portfolio.PortfolioPosition getPortfolioPosition(String figi) {
        try {
            return modelController.getPortfolioPosition(figi);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    public Portfolio.PortfolioPosition getSelected() {
        return modelController.getSelected();
    }
}
