package com.tukaloff.tradeterminal.controller;

import com.tukaloff.tradeterminal.model.TerminalModel;
import com.tukaloff.tradeterminal.service.InvestOpenapiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ModelController {

    private TerminalModel terminalModel;

    @Autowired
    private InvestOpenapiService investOpenapiService;

    public ModelController() {
        terminalModel = new TerminalModel();
    }

    public List<Portfolio.PortfolioPosition> getPortfolioPositions() {
        try {
            List<Portfolio.PortfolioPosition> portfolioPositions = investOpenapiService.getPortfolioPositions();
            terminalModel.setPortfolioPositions(portfolioPositions);
            terminalModel.setSelected(portfolioPositions.stream().findFirst().get());
            return portfolioPositions;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        return Collections.EMPTY_LIST;
    }

    public Portfolio.PortfolioPosition getPortfolioPosition(String figi) {
        try {
            Portfolio.PortfolioPosition portfolioPosition = investOpenapiService.getPortfolioPosition(figi);
            terminalModel.setSelected(portfolioPosition);
            return portfolioPosition;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    public Portfolio.PortfolioPosition getSelected() {
        return terminalModel.getSelected();
    }
}
