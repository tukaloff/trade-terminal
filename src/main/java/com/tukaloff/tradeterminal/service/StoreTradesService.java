package com.tukaloff.tradeterminal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.tukaloff.tradeterminal.model.TradePosition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class StoreTradesService {

    ObjectMapper mapper = new JsonMapper();

    @Value("${trades.file}")
    private String fileName;

    public void save(List<TradePosition> tradePosition) {
        try {
            mapper.writeValueAsString(tradePosition);
            Files.writeString(Paths.get(fileName), mapper.writeValueAsString(tradePosition));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<TradePosition> load() {
        try {
            String s = Files.readString(Path.of(fileName));
            TradePosition[] trades = mapper.readValue(s, TradePosition[].class).clone();
            return new ArrayList<>(Arrays.asList(trades));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
