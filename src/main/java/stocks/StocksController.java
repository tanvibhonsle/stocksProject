package stocks;

/**
 * Created by tanvi.bhonsle on 12/06/17.
 */

import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanToCsv;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * Controller mapping to parent URL. Read txt file with stock codes and gets stock data details from
 * Yahoo finance API for those stock codes
 */
@RestController
public class StocksController {

    private static final Logger logger = LoggerFactory.getLogger(StocksController.class);

    private static final BigDecimal ERROR_VALUE = BigDecimal.valueOf(-1.0);

    private static ExecutorService executor = Executors.newFixedThreadPool(20);

    @Value("${app.inputFilePath}")
    private String inputFilePath;

    @Value("${app.outputFilePath}")
    private String outputFilePath;

    @RequestMapping("/")
    public String stocksInformation() {
        try {
            // Read data from input text file
            Stream<String> inputStockCodesStream = Files.lines(Paths.get(inputFilePath));

            //List that stores the required data from the Yahoo API
            List<StockInformation> stockData = new ArrayList<>();

            //Executor service for parallel processing of requests to Yahoo finance
            Set<Callable<String>> callableList = new HashSet<Callable<String>>();
            inputStockCodesStream.forEach(inputStockCode -> {
                callableList.add(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        Stock stock = getStockInformationFromYahoo(inputStockCode);
                        StockInformation stockInformation = new StockInformation();
                        stockInformation.setStockCode(inputStockCode);
                        if (stock != null) {
                            stockInformation = mapStockInformationToBean(stock, stockInformation);
                        } else {
                            stockInformation = addErrorDataToStockInformation(stockInformation);
                        }
                        stockData.add(stockInformation);
                        return "Done";
                    }
                });
            });
            List<Future<String>> futures = executor.invokeAll(callableList);
            executor.shutdown();

            //Create csv for output data
            CSVWriter writer = null;
            writer = new CSVWriter(new FileWriter(outputFilePath));
            BeanToCsv bc = new BeanToCsv();
            ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
            mappingStrategy.setType(StockInformation.class);
            String[] columns = new String[]{"stockCode","currentPrice","targetPrice", "yearHigh", "yearLow"};
            mappingStrategy.setColumnMapping(columns);
            bc.write(mappingStrategy,writer,stockData);
            writer.close();

            logger.info("CSV File written successfully!!!");
            return "CSV File written successfully!!!";
        } catch(Exception ex) {
            logger.error(ex.getMessage());
            logger.error("Error in StocksController");
            return "Error in StocksController";
        }
    }

    /**
     * @param stockInformation
     * @return stockInformation
     */
    private StockInformation addErrorDataToStockInformation(StockInformation stockInformation) {
        try {
            logger.debug(" Populating error data for stockcode " + stockInformation.getStockCode());
            stockInformation.setCurrentPrice(ERROR_VALUE);
            stockInformation.setTargetPrice(ERROR_VALUE);
            stockInformation.setYearHigh(ERROR_VALUE);
            stockInformation.setYearLow(ERROR_VALUE);
            return stockInformation;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            logger.error("Error in addErrorDataToStockInformation for stockCode " + stockInformation.getStockCode());
            return null;
        }
    }

    /**
     * @param stock
     * @param stockInformation
     * @return stockInformation
     */
    private StockInformation mapStockInformationToBean(Stock stock, StockInformation stockInformation) {
        try {
            logger.debug(" Creating beans for populating csv for stockCode " + stockInformation.getStockCode());
            BigDecimal price = stock.getQuote().getPrice();
            if (price != null) {
                stockInformation.setCurrentPrice(price);
            } else {
                stockInformation.setCurrentPrice(ERROR_VALUE);
            }
            BigDecimal yearHigh = stock.getQuote().getYearHigh();
            if (yearHigh != null) {
                stockInformation.setYearHigh(yearHigh);
            } else {
                stockInformation.setYearHigh(ERROR_VALUE);
            }
            BigDecimal yearLow = stock.getQuote().getYearLow();
            if (yearLow != null) {
                stockInformation.setYearLow(yearLow);
            } else {
                stockInformation.setYearLow(ERROR_VALUE);
            }
            BigDecimal oneYearTargetPrice = stock.getStats().getOneYearTargetPrice();
            if (oneYearTargetPrice != null) {
                stockInformation.setTargetPrice(oneYearTargetPrice);
            } else {
                stockInformation.setTargetPrice(ERROR_VALUE);
            }
            return stockInformation;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            logger.error("Error in mapStockInformationToBean for stockCode " + stockInformation.getStockCode());
            return null;
        }
    }

    /**
     * @param stockCode
     * @return
     */
    @Cacheable("stocksCache")
    private Stock getStockInformationFromYahoo(String stockCode) {
        try {
            Stock stock = YahooFinance.get(stockCode);
            logger.debug("finance data fetched for stock code: " + stockCode);
            return stock;
        } catch (IOException e) {
            logger.error("Error while fetching finance data from Yahoo finance");
            logger.error(e.getMessage());
            return null;
        }
    }

}
