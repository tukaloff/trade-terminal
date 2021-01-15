package com.tukaloff.tradeterminal.view;

import com.tukaloff.tradeterminal.controller.ViewController;
import lombok.extern.slf4j.Slf4j;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class PortfolioPositionList extends JPanel implements ChangeListener {

    private List<PortfolioPositionItem> portfolioPositionItemList;
    private ViewController viewController;

    public PortfolioPositionList(ViewController viewController) {
        super(new GridLayout(0, 1));
        this.viewController = viewController;
        this.portfolioPositionItemList = new ArrayList<>();
        viewController.addListener(this, Component.PORTFOLIO_POSITION_LIST_LISTENER);
    }

    public void addPortfolioPositions(List<Portfolio.PortfolioPosition> portfolioPositions) {
        portfolioPositions.forEach(this::addPortfolioPosition);
    }

    public void addPortfolioPosition(Portfolio.PortfolioPosition position) {
        PortfolioPositionItem item = new PortfolioPositionItem(position);
        addPortfolioPositionItem(item);
        this.portfolioPositionItemList.add(item);
    }

    public void addPortfolioPositionItem(PortfolioPositionItem item) {
        LayerUI<JPanel> layerUI = new LayerUI<>(){

            public void installUI(JComponent c) {
                super.installUI(c);
                // enable mouse motion events for the layer's subcomponents
                ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
            }

            public void uninstallUI(JComponent c) {
                super.uninstallUI(c);
                // reset the layer event mask
                ((JLayer) c).setLayerEventMask(0);
            }

            public void eventDispatched(AWTEvent e, JLayer<? extends JPanel> l) {
                if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                    PortfolioPositionItem component = (PortfolioPositionItem) (l.getView());
                    log.info(component.getPosition().name);
                    viewController.select(component);
                }
            }
        };
        JLayer<JPanel> jlayer = new JLayer<>(item, layerUI);
        this.add(jlayer);
    }

    @Override
    public void redraw(Object change) {
//        Arrays.stream(this.getComponents()).forEach(this::remove);
        List<Portfolio.PortfolioPosition> list = (List<Portfolio.PortfolioPosition>) change;
        list.forEach(position -> {
            Optional<PortfolioPositionItem> first = portfolioPositionItemList.stream()
                    .filter(item -> position.figi.equals(item.getPosition().figi))
                    .findFirst();
            if (first.isPresent()) {
                first.get().update(position);
            } else {
                this.addPortfolioPosition(position);
            }
        });
        revalidate();
        repaint();
    }
}
