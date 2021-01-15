package com.tukaloff.tradeterminal.view;

import com.tukaloff.tradeterminal.controller.ViewController;
import com.tukaloff.tradeterminal.model.Instrument;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class CommonViewPanel extends JPanel implements ChangeListener {

    private final ViewController viewController;

    public CommonViewPanel(ViewController viewController) {
        super(new GridLayout(1, 0));
        this.viewController = viewController;
        viewController.addListener(this, Component.COMMON_VIEW_LISTENER);
    }

    @Override
    public void redraw(Object change) {
        if (change != null) {
            Instrument selected = (Instrument) change;
            Arrays.stream(this.getComponents()).forEach(this::remove);
            this.add(new Label(selected.getPortfolioPosition().name));
            this.add(new Label(selected.getLastCandle().closePrice.toEngineeringString()));
            revalidate();
            repaint();
        }
    }
}
