package com.tukaloff.tradeterminal.view;

import lombok.Getter;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Arrays;

@Getter
public class PortfolioPositionItem extends JPanel {

    private Portfolio.PortfolioPosition position;
    private JLabel nameLabel;
    private JLabel balanceLabel;
    private JLabel totalLabel;
    private JLabel profitLabel;

    public PortfolioPositionItem(Portfolio.PortfolioPosition position) {
        super(new GridLayout(1, 0));
        this.position = position;
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        if (position.expectedYield.value.compareTo(BigDecimal.ZERO) >= 0)
            this.setBackground(Color.GREEN);
        else
            this.setBackground(Color.PINK);
        create(position);
    }

    public void create(Portfolio.PortfolioPosition position) {
        Arrays.stream(this.getComponents()).forEach(this::remove);
        JPanel left = new JPanel(new GridLayout(0, 1));
        this.add(left);
        left.setBackground(left.getParent().getBackground());
        this.nameLabel = new JLabel(position.name);
        left.add(nameLabel);
        JPanel left_bot = new JPanel(new GridLayout(1, 0));
        left.add(left_bot);
        left_bot.setBackground(left_bot.getParent().getBackground());
        this.balanceLabel = new JLabel(position.balance.toEngineeringString()
                + " x " + position.averagePositionPrice.value.toEngineeringString()
                + " " + position.averagePositionPrice.currency.name());
        left_bot.add(balanceLabel);
        JPanel right = new JPanel(new GridLayout(0, 1));
        this.add(right);
        right.setBackground(right.getParent().getBackground());
        JPanel right_up = new JPanel(new GridLayout(1, 0));
        right.add(right_up);
        right_up.setBackground(right_up.getParent().getBackground());
        BigDecimal total = position.averagePositionPrice.value.multiply(position.balance);
        totalLabel = new JLabel(total.toEngineeringString()
                + " " + position.averagePositionPrice.currency.name());
        right_up.add(totalLabel);
        profitLabel = new JLabel(position.expectedYield.value.toEngineeringString());
        right.add(profitLabel);
        revalidate();
        repaint();
    }

    public void update(Portfolio.PortfolioPosition position) {
        nameLabel.setText(position.name);
        this.balanceLabel.setText(position.balance.toEngineeringString()
                + " x " + position.averagePositionPrice.value.toEngineeringString()
                + " " + position.averagePositionPrice.currency.name());
        BigDecimal total = position.averagePositionPrice.value.multiply(position.balance);
        totalLabel.setText(total.toEngineeringString()
                + " " + position.averagePositionPrice.currency.name());
        profitLabel.setText(position.expectedYield.value.toEngineeringString());
        if (position.expectedYield.value.compareTo(BigDecimal.ZERO) >= 0)
            this.setBackground(Color.GREEN);
        else
            this.setBackground(Color.PINK);
        repaint();
    }
}
