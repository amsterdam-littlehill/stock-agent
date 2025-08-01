package com.jd.genie.service.data;

import com.jd.genie.config.StockDataConfig;
import com.jd.genie.entity.StockData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 新浪财经数据客户端
 * 提供实时股票数据获取功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SinaStockDataClient implements StockDataClient {

    private final StockDataConfig stockDataConfig;
    private final RestTemplate restTemplate;

    private static final String REAL_TIME_URL = "/list=";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<StockData> getRealTimeData(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 转换股票代码格式（如：000001 -> sz000001）
            String symbolsParam = symbols.stream()
                    .map(this::convertSymbolFormat)
                    .collect(Collectors.joining(","));

            String url = stockDataConfig.getSina().getBaseUrl() + REAL_TIME_URL + symbolsParam;
            log.debug("请求新浪财经API: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            return parseRealTimeResponse(response, symbols);

        } catch (ResourceAccessException e) {
            log.error("新浪财经API请求超时: {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("获取新浪财经实时数据失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public StockData getRealTimeData(String symbol) {
        List<StockData> dataList = getRealTimeData(Collections.singletonList(symbol));
        return dataList.isEmpty() ? null : dataList.get(0);
    }

    @Override
    public boolean isEnabled() {
        return stockDataConfig.getSina().isEnabled();
    }

    @Override
    public String getDataSource() {
        return "SINA";
    }

    /**
     * 转换股票代码格式
     * 000001.SZ -> sz000001
     * 600000.SH -> sh600000
     */
    private String convertSymbolFormat(String symbol) {
        if (symbol.contains(".")) {
            String[] parts = symbol.split("\\.");
            String code = parts[0];
            String market = parts[1].toLowerCase();
            return market + code;
        }
        // 默认处理：6开头为上海，其他为深圳
        return symbol.startsWith("6") ? "sh" + symbol : "sz" + symbol;
    }

    /**
     * 解析新浪财经实时数据响应
     */
    private List<StockData> parseRealTimeResponse(String response, List<String> originalSymbols) {
        if (response == null || response.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<StockData> result = new ArrayList<>();
        String[] lines = response.split("\n");
        
        for (int i = 0; i < lines.length && i < originalSymbols.size(); i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            try {
                StockData stockData = parseSingleStockData(line, originalSymbols.get(i));
                if (stockData != null) {
                    result.add(stockData);
                }
            } catch (Exception e) {
                log.warn("解析股票数据失败: {} - {}", originalSymbols.get(i), e.getMessage());
            }
        }

        return result;
    }

    /**
     * 解析单个股票数据
     * 新浪数据格式：var hq_str_sz000001="平安银行,9.170,9.190,9.190,9.270,9.150,9.180,9.190,81058825,743902226.000,316100,9.180,155200,9.170,85800,9.160,57300,9.150,96800,9.140,90100,9.190,85800,9.200,69400,9.210,62500,9.220,44200,9.230,2023-12-08,15:00:00,00,";
     */
    private StockData parseSingleStockData(String line, String originalSymbol) {
        // 提取引号内的数据
        int startIndex = line.indexOf('"');
        int endIndex = line.lastIndexOf('"');
        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            return null;
        }

        String dataStr = line.substring(startIndex + 1, endIndex);
        String[] fields = dataStr.split(",");
        
        if (fields.length < 32) {
            log.warn("新浪数据字段不足: {} fields, expected >= 32", fields.length);
            return null;
        }

        try {
            StockData stockData = new StockData();
            stockData.setSymbol(originalSymbol);
            stockData.setName(fields[0]);
            stockData.setOpenPrice(new BigDecimal(fields[1]));
            stockData.setPrevClose(new BigDecimal(fields[2]));
            stockData.setCurrentPrice(new BigDecimal(fields[3]));
            stockData.setHighPrice(new BigDecimal(fields[4]));
            stockData.setLowPrice(new BigDecimal(fields[5]));
            
            // 计算涨跌额和涨跌幅
            BigDecimal currentPrice = stockData.getCurrentPrice();
            BigDecimal prevClose = stockData.getPrevClose();
            if (prevClose.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal changeAmount = currentPrice.subtract(prevClose);
                BigDecimal changePercent = changeAmount.divide(prevClose, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                stockData.setChangeAmount(changeAmount);
                stockData.setChangePercent(changePercent);
            }

            stockData.setVolume(Long.parseLong(fields[8]));
            stockData.setTurnover(new BigDecimal(fields[9]));
            
            // 解析时间
            String dateStr = fields[30];
            String timeStr = fields[31];
            if (!dateStr.isEmpty() && !timeStr.isEmpty()) {
                String dateTimeStr = dateStr + " " + timeStr;
                stockData.setUpdateTime(LocalDateTime.parse(dateTimeStr, TIME_FORMATTER));
            } else {
                stockData.setUpdateTime(LocalDateTime.now());
            }
            
            stockData.setDataSource(getDataSource());
            stockData.setTradingStatus("TRADING");
            stockData.setIsLatest(true);

            return stockData;
        } catch (Exception e) {
            log.error("解析新浪股票数据失败: {}", originalSymbol, e);
            return null;
        }
    }

    /**
     * 批量获取股票基本信息
     */
    public List<StockData> getStockBasicInfo(List<String> symbols) {
        // 新浪API同时提供基本信息，直接调用实时数据接口
        return getRealTimeData(symbols);
    }

    /**
     * 检查API可用性
     */
    public boolean checkApiAvailability() {
        try {
            // 使用平安银行作为测试股票
            StockData testData = getRealTimeData("000001.SZ");
            return testData != null && testData.getCurrentPrice() != null;
        } catch (Exception e) {
            log.warn("新浪财经API不可用: {}", e.getMessage());
            return false;
        }
    }
}