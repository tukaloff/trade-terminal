package com.tukaloff.tradeterminal.view;

import com.tukaloff.tradeterminal.controller.ViewController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class MainForm extends JFrame {

    @Autowired
    private ViewController viewController;
    private JPanel center;

    @PostConstruct
    private void init() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBounds((int)(screenSize.width * 0.15),
                (int)(screenSize.height * 0.25),
                (int)(screenSize.width * 0.7),
                (int)(screenSize.height * 0.5));
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);

        List<Portfolio.PortfolioPosition> portfolioPositions = viewController.getPortfolioPositions();

        JPanel positions = new JPanel(new GridLayout(0, 1));
        mainPanel.add(positions, BorderLayout.WEST);
        portfolioPositions.forEach(portfolioPosition -> {
            PortfolioPositionItem item = new PortfolioPositionItem(portfolioPosition);
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
                        component.update(viewController.getPortfolioPosition(component.getPosition().figi));
                        changeSelected();
                    }
                }
            };
            JLayer<JPanel> jlayer = new JLayer<>(item, layerUI);
            positions.add(jlayer);
        });

        center = new JPanel(new GridLayout(0, 1));
        mainPanel.add(center, BorderLayout.CENTER);
        repaintCenter();


        this.setVisible(true);
    }

    private void changeSelected() {
        repaintCenter();
    }

    private void repaintCenter() {
        Arrays.stream(center.getComponents()).forEach(component -> center.remove(component));
        JPanel top_center = new JPanel();
        center.add(top_center);
        JPanel bottom_center = new JPanel();
        center.add(bottom_center);
        top_center.add(new Label(viewController.getSelected().name));
    }
}
