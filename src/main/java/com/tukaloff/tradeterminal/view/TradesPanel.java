package com.tukaloff.tradeterminal.view;

import com.tukaloff.tradeterminal.controller.ViewController;
import com.tukaloff.tradeterminal.model.Instrument;
import com.tukaloff.tradeterminal.model.TradePosition;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public class TradesPanel extends JPanel implements ChangeListener, SelectionListener {

    private final ViewController viewController;
    private JPanel tradesPanel;
    private JTable table;
    private TextField tickerTextField;
    private TextField openValueTextField;

    public TradesPanel(ViewController viewController) {
        super(new BorderLayout());
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.viewController = viewController;
        tradesPanel = new JPanel(new GridLayout(0, 1));
        this.add(tradesPanel, BorderLayout.NORTH);
        String[] header = new String[]{"Date", "Tiker", "Buy", "Fee", "MinSell", "Profit"};
        DefaultTableModel defaultTableModel = new DefaultTableModel();
        defaultTableModel.setColumnIdentifiers(header);
        defaultTableModel.addRow(header);
        table = new JTable(defaultTableModel);
        this.add(table, BorderLayout.CENTER);
        viewController.addListener(this, Component.TRADE_POSITIONS_LISTENER);
        viewController.addSelectionListener(this);
        addAddingTradePanel();
    }

    @Override
    public void redraw(Object change) {
        ArrayList<TradePosition> tradePositions = (ArrayList<TradePosition>) change;
        log.info((long) tradePositions.size() + "");
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        // todo: fix Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException: 2 >= 1
        model.getDataVector().removeAllElements();
        tradePositions.stream()
                .filter(tradePosition ->
                        Objects.equals(viewController.getSelected().getPortfolioPosition().ticker, tradePosition.getTicker()))
                .forEach(this::addTradePosition);
    }

    private void addTradePosition(TradePosition tradePosition) {
        var lastCandle = viewController.getSelected().getLastCandle();
        var model = (DefaultTableModel) table.getModel();
        model.addRow(new String[]{
                tradePosition.getOpenDate(),
                tradePosition.getTicker(),
                tradePosition.getOpenValue().toEngineeringString(),
                tradePosition.getOpenFeeValue().toEngineeringString(),
                tradePosition.getMinimalSellPrice().toEngineeringString(),
                lastCandle.closePrice.subtract(tradePosition.getMinimalSellPrice()).toEngineeringString()
        });
    }

    private void addAddingTradePanel() {
        Instrument instrument;
        String ticker = "";
        String openValue = "";
        String openFeePerc = "0.0005";
        String closeFeePerc = "0.0005";
        if (viewController.getSelected() != null) {
            instrument = viewController.getSelected();
            ticker = instrument.getPortfolioPosition().ticker;
            openValue = instrument.getLastCandle().closePrice.toEngineeringString();
        }

        JPanel addTradePanel = new JPanel(new FlowLayout());
        tradesPanel.add(addTradePanel);
        var saveButton = new JButton();
        addTradePanel.add(saveButton);
        this.tickerTextField = new TextField(ticker);
        addTradePanel.add(tickerTextField);
        this.openValueTextField = new TextField(openValue);
        addTradePanel.add(openValueTextField);
        var openFeePercTextField = new TextField(openFeePerc);
        addTradePanel.add(openFeePercTextField);
        var closeFeePercTextField = new TextField(closeFeePerc);
        addTradePanel.add(closeFeePercTextField);
        saveButton.addActionListener(e -> {
            String tickerText = tickerTextField.getText();
            String openValueText = openValueTextField.getText();
            String openFeePercText = openFeePercTextField.getText();
            String closeFeePercText = closeFeePercTextField.getText();
            log.info("pressed: {}, {}", tickerText, openValueText);
            viewController.createTradePosition(openValueText, openFeePercText, closeFeePercText, tickerText);
        });
//        JSlider slider = new JSlider();
//        addTradePanel.add(slider);
//        slider.setMaximum(24);
//        slider.setMinimum(1);
//        slider.setValue(1);
//        slider.setPaintLabels(true);
//        slider.setMajorTickSpacing(1);
//        slider.setSnapToTicks(true);
//        slider.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                viewController.zoomPlot(slider.getValue());
//            }
//        });
    }

    @Override
    public void onSelect() {
        this.tickerTextField.setText(viewController.getSelected().getPortfolioPosition().ticker);
        this.openValueTextField.setText(viewController.getSelected().getLastCandle().closePrice.toEngineeringString());
    }
}
