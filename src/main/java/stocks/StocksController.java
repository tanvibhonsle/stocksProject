package stocks;

/**
 * Created by tanvi.bhonsle on 12/06/17.
 */

import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanToCsv;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Controller mapping to parent URL. Read txt file with stock codes and gets stock data details from
 * Yahoo finance API for those stock codes
 */
@RestController
public class StocksController {

    private static final Logger logger = LoggerFactory.getLogger(StocksController.class);

//    @RequestMapping("/")
//    public String stocksInformation() {
//        try {
//            // Read data from input text file
//            Stream<String> streams = Files.lines(Paths.get("/Users/tanvi.bhonsle/Downloads/Stocks.txt"));
//
//            //Create csv for output data
//            String csv = "/Users/tanvi.bhonsle/Downloads/output.csv";
//
//            CSVWriter writer = new CSVWriter(new FileWriter(csv));
//            //List that stores the required data from the Yahoo API
//            List<StockInformation> stockData = new ArrayList<>();
//            final Integer[] idCounter = {1};
//            streams.forEach(stream -> {
//                StockInformation stockInformation = new StockInformation();
//                stockInformation.setId(idCounter[0]++);
//                stockInformation.setStockCode(stream);
//                Stock stock = null;
//                try {
//                    stock = YahooFinance.get(stream);
//                    logger.debug("finance data fetched for stock code: " + stream);
//                } catch (IOException e) {
//                    System.out.print("Error while fetching finance data");
//                }
//                BigDecimal price = stock.getQuote().getPrice();
//                stockInformation.setCurrentPrice(price);
//                BigDecimal yearHigh = stock.getQuote().getYearHigh();
//                stockInformation.setYearHigh(yearHigh);
//                BigDecimal yearLow = stock.getQuote().getYearLow();
//                stockInformation.setYearLow(yearLow);
//                BigDecimal oneYearTargetPrice = stock.getStats().getOneYearTargetPrice();
//                stockInformation.setTargetPrice(oneYearTargetPrice);
//
//                stockData.add(stockInformation);
//            });
//
//            BeanToCsv bc = new BeanToCsv();
//            ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
//
//            mappingStrategy.setType(StockInformation.class);
//
//            String[] columns = new String[]{"Id","stockCode","currentPrice","targetPrice", "yearHigh", "yearLow"};
//
//            mappingStrategy.setColumnMapping(columns);
//
//            bc.write(mappingStrategy,writer,stockData);
//            System.out.println("CSV File written successfully!!!");
//            logger.debug("Welcome {}", "testing");
//
//            writer.close();
//
////            model.addAttribute("message", "CSV File written successfully!!!");
//            return "CSV File written successfully!!!";
//        }
//        catch(IOException ex) {
//            System.out.print("Error");
//            return "Error";
//        }
//    }

    @RequestMapping("/")
    public String stocksInformation() throws InterruptedException {
        try {
            // Read data from input text file
            Stream<String> streams = Files.lines(Paths.get("/Users/tanvi.bhonsle/Downloads/Stocks.txt"));

            //Create csv for output data
            String csv = "/Users/tanvi.bhonsle/Downloads/output.csv";

            CSVWriter writer = new CSVWriter(new FileWriter(csv));
            //List that stores the required data from the Yahoo API
            List<StockInformation> stockData = new ArrayList<>();
            final Integer[] idCounter = {1};

            ExecutorService executor = Executors.newFixedThreadPool(10);
            Collection callableList = new LinkedList<>();
            streams.forEach(stream -> {
                callableList.add(new Callable() {
                    @Override
                    public Boolean call() throws Exception {
                        StockInformation stockInformation = new StockInformation();
                        stockInformation.setId(idCounter[0]++);
                        stockInformation.setStockCode(stream);
                        Stock stock = null;
                        try {
                            stock = YahooFinance.get(stream);
                            logger.debug("finance data fetched for stock code: " + stream);
                        } catch (IOException e) {
                            System.out.print("Error while fetching finance data");
                        }
                        BigDecimal price = stock.getQuote().getPrice();
                        stockInformation.setCurrentPrice(price);
                        BigDecimal yearHigh = stock.getQuote().getYearHigh();
                        stockInformation.setYearHigh(yearHigh);
                        BigDecimal yearLow = stock.getQuote().getYearLow();
                        stockInformation.setYearLow(yearLow);
                        BigDecimal oneYearTargetPrice = stock.getStats().getOneYearTargetPrice();
                        stockInformation.setTargetPrice(oneYearTargetPrice);

                        stockData.add(stockInformation);
                        return true;
                    }
                });
            });
            List futures = executor.invokeAll(callableList);

            executor.shutdown();

//            streams.forEach(stream -> {
//                StockInformation stockInformation = new StockInformation();
//                stockInformation.setId(idCounter[0]++);
//                stockInformation.setStockCode(stream);
//                Stock stock = null;
//                try {
//                    stock = YahooFinance.get(stream);
//                    logger.debug("finance data fetched for stock code: " + stream);
//                } catch (IOException e) {
//                    System.out.print("Error while fetching finance data");
//                }
//                BigDecimal price = stock.getQuote().getPrice();
//                stockInformation.setCurrentPrice(price);
//                BigDecimal yearHigh = stock.getQuote().getYearHigh();
//                stockInformation.setYearHigh(yearHigh);
//                BigDecimal yearLow = stock.getQuote().getYearLow();
//                stockInformation.setYearLow(yearLow);
//                BigDecimal oneYearTargetPrice = stock.getStats().getOneYearTargetPrice();
//                stockInformation.setTargetPrice(oneYearTargetPrice);
//
//                stockData.add(stockInformation);
//            });

            BeanToCsv bc = new BeanToCsv();
            ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();

            mappingStrategy.setType(StockInformation.class);

            String[] columns = new String[]{"Id","stockCode","currentPrice","targetPrice", "yearHigh", "yearLow"};

            mappingStrategy.setColumnMapping(columns);

            bc.write(mappingStrategy,writer,stockData);
            System.out.println("CSV File written successfully!!!");
            logger.debug("Welcome {}", "testing");

            writer.close();

//            model.addAttribute("message", "CSV File written successfully!!!");
            return "CSV File written successfully!!!";
        }
        catch(IOException ex) {
            System.out.print("Error");
            return "Error";
        } catch(Exception ex) {
            System.out.print("Error");
            return "Error in threading";
        }
    }

}
