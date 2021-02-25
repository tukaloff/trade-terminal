package com.tukaloff.tradeterminal.view;

import com.tukaloff.tradeterminal.controller.ViewController;

import javax.swing.*;
import java.awt.*;

public class CommonViewPanel extends JPanel implements ChangeListener {

    private final ViewController viewController;

    public CommonViewPanel(ViewController viewController) {
        super(new GridLayout(1, 0));
        this.viewController = viewController;
        PlotPanel plot = new PlotPanel(viewController);
        this.add(plot);
        viewController.addListener(this, Component.COMMON_VIEW_LISTENER);
    }

    @Override
    public void redraw(Object change) {

    }
}
