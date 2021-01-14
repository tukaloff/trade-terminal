package com.tukaloff.tradeterminal.model;

import lombok.Getter;
import lombok.Setter;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.util.List;

@Getter
@Setter
public class TerminalModel {

    List<Portfolio.PortfolioPosition> portfolioPositions;
    Portfolio.PortfolioPosition selected;
}
