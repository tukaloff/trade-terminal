package com.tukaloff.tradeterminal.view;

import com.tukaloff.tradeterminal.controller.ViewController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

@Slf4j
@Service
public class MainForm extends JFrame {

    @Autowired
    private ViewController viewController;
    private JPanel center;

    @PostConstruct
    private void init() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBounds((int)(screenSize.width * 0.15),
                (int)(screenSize.height * 0.25),
                (int)(screenSize.width * 0.7),
                (int)(screenSize.height * 0.5));
        var mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);

        var list = new PortfolioPositionList(viewController);
        mainPanel.add(list, BorderLayout.WEST);

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
        JPanel top_center = new CommonViewPanel(viewController);
        center.add(top_center);
        JPanel bottom_center = new TradesFrame(viewController);
        center.add(bottom_center);
//        top_center.add(new Label(viewController.getSelected().name));
    }
}
