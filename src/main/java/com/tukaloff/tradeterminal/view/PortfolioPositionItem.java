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

    public PortfolioPositionItem(Portfolio.PortfolioPosition position) {
        super(new GridLayout(1, 0));
        this.position = position;
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        if (position.expectedYield.value.compareTo(BigDecimal.ZERO) >= 0)
            this.setBackground(Color.GREEN);
        else
            this.setBackground(Color.PINK);
        update(position);
    }

    public void update(Portfolio.PortfolioPosition position) {
        Arrays.stream(this.getComponents()).forEach(this::remove);
        JPanel left = new JPanel(new GridLayout(0, 1));
        this.add(left);
        left.setBackground(left.getParent().getBackground());
        left.add(new Label(position.name));
        JPanel left_bot = new JPanel(new GridLayout(1, 0));
        left.add(left_bot);
        left_bot.setBackground(left_bot.getParent().getBackground());
        left_bot.add(new Label(position.balance.toEngineeringString()
                + " x " + position.averagePositionPrice.value.toEngineeringString()
                + " " + position.averagePositionPrice.currency.name()));
        JPanel right = new JPanel(new GridLayout(0, 1));
        this.add(right);
        right.setBackground(right.getParent().getBackground());
        JPanel right_up = new JPanel(new GridLayout(1, 0));
        right.add(right_up);
        right_up.setBackground(right_up.getParent().getBackground());
        BigDecimal total = position.averagePositionPrice.value.multiply(position.balance);
        right_up.add(new Label(total.toEngineeringString()
                + " " + position.averagePositionPrice.currency.name()));
        right.add(new Label(position.expectedYield.value.toEngineeringString()));
        revalidate();
        repaint();
    }
}
