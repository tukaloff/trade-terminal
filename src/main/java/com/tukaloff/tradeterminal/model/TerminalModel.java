package com.tukaloff.tradeterminal.model;

import lombok.Getter;
import lombok.Setter;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.util.List;

@Getter
@Setter
public class TerminalModel {

    private List<Portfolio.PortfolioPosition> portfolioPositions;
    private Instrument selected;
    private List<TradePosition> tradePositions;
    private int depth;
}
