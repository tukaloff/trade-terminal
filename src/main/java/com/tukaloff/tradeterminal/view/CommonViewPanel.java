package com.tukaloff.tradeterminal.view;

import com.tukaloff.tradeterminal.controller.ViewController;

import javax.swing.*;
import java.awt.*;

public class CommonViewPanel extends JPanel implements ChangeListener {

    private final ViewController viewController;
//    private final JLabel tickerName;
//    private final JLabel closePrice;

    public CommonViewPanel(ViewController viewController) {
        super(new GridLayout(1, 0));
        this.viewController = viewController;
        PlotPanel plot = new PlotPanel(viewController);
        this.add(plot);
//        tickerName = new JLabel();
//        this.add(tickerName);
//        closePrice = new JLabel();
//        this.add(closePrice);
        viewController.addListener(this, Component.COMMON_VIEW_LISTENER);
    }

    @Override
    public void redraw(Object change) {
//        if (change != null) {
//            Instrument selected = (Instrument) change;
//            tickerName.setText(selected.getPortfolioPosition().name);
//            closePrice.setText(selected.getLastCandle().closePrice.toEngineeringString());
//            repaint();
//        }
    }
}
