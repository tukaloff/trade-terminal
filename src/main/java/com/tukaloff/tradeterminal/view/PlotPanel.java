package com.tukaloff.tradeterminal.view;

import com.tukaloff.tradeterminal.controller.ViewController;
import com.tukaloff.tradeterminal.model.Instrument;
import com.tukaloff.tradeterminal.model.TradePosition;
import lombok.extern.slf4j.Slf4j;
import ru.tinkoff.invest.openapi.models.market.Candle;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
public class PlotPanel extends JPanel implements ChangeListener {

    private final ViewController viewController;

    public PlotPanel(ViewController viewController) {
        this.viewController = viewController;
        viewController.addListener(this, Component.CANDLES_LISTENER);
        this.addMouseWheelListener(e -> viewController.zoomPlot(e.getWheelRotation()));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponents(g);
        Instrument selected = viewController.getSelected();
        if (selected == null) return;
        int fromIndex = selected.getCandles().size() - viewController.getZoom() * 60;
        if (fromIndex < 0) fromIndex = 0;
        List<Candle> candles = selected.getCandles()
                .subList(fromIndex, selected.getCandles().size());
        double candlesCount = candles.size();
        Optional<Candle> min = candles.stream().min(Comparator.comparing(o -> o.lowestPrice));
        if (min.isEmpty()) return;
        double lowest = min.get().lowestPrice.doubleValue();
        Optional<Candle> max = candles.stream().max(Comparator.comparing(o -> o.highestPrice));
        double highest = max.get().highestPrice.doubleValue();
        Graphics2D g2 = (Graphics2D) g;
        Rectangle r = getBounds();
        g2.setBackground(Color.DARK_GRAY);
        g2.clearRect(0, 0, r.width, r.height);
        g2.setPaint(Color.GREEN);
        List<TradePosition> tradePositions = viewController.getTradePositions();
        tradePositions.stream()
                .filter(tradePosition -> tradePosition.getMinimalSellPrice().doubleValue() < highest
                        && tradePosition.getMinimalSellPrice().doubleValue() > lowest)
                .forEach(tradePosition -> {
                    int yCord = (int) (r.getBounds().height - (tradePosition.getMinimalSellPrice().doubleValue() - lowest) / (highest - lowest) * r.getBounds().height);
                    g2.drawLine(0,yCord, r.width, yCord);
                });
        for (int i = 0; i < candlesCount; i++) {
            Candle candle = candles.get(i);
            int xCord = (int) (r.getBounds().width / candlesCount * i + r.getWidth() / candlesCount / 4);
            int yCord = (int) (r.getBounds().height - (candle.closePrice.doubleValue() - lowest) / (highest - lowest) * r.getBounds().height);
            double height = (candle.closePrice.subtract(candle.openPrice).doubleValue()) / (highest - lowest) * r.getBounds().height;
            if (height < 0) {
                g2.setPaint(Color.RED);
                height = -height;
            } else {
                g2.setPaint(Color.GREEN);
            }
            g2.fillRect(xCord, yCord, (int) (r.getWidth() / candlesCount / 2), (int) height);
            g2.drawLine((int) (xCord + r.getWidth() / candlesCount / 4),
                    (int) (r.getBounds().height - (candle.highestPrice.doubleValue() - lowest) / (highest - lowest) * r.getBounds().height),
                    (int) (xCord + r.getWidth() / candlesCount / 4),
                    (int) (r.getBounds().height - (candle.lowestPrice.doubleValue() - lowest) / (highest - lowest) * r.getBounds().height));
        }
        Candle lastCandle = selected.getLastCandle();
        if (lastCandle.closePrice.doubleValue() > lastCandle.openPrice.doubleValue()) {
            g2.setPaint(Color.GREEN);
        } else {
            g2.setPaint(Color.RED);
        }
        Optional<Portfolio.PortfolioPosition> portfolioPosition = viewController.getPortfolioPositions().stream().filter(position -> position.figi.equals(lastCandle.figi)).findFirst();
        String tickerString = "";
        if (portfolioPosition.isPresent()) {
            tickerString = portfolioPosition.get().name + " (" + portfolioPosition.get().ticker + ") ";
        }
        tickerString += "O " + lastCandle.openPrice + " ";
        tickerString += "H " + lastCandle.highestPrice + " ";
        tickerString += "L " + lastCandle.lowestPrice + " ";
        tickerString += "C " + lastCandle.closePrice + " ";
        tickerString += "T " + lastCandle.tradesValue + " ";
        g2.drawString(tickerString, 10, 50);
    }

    @Override
    public void redraw(Object change) {
        repaint();
    }
}
