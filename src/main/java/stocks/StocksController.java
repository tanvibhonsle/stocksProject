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

    private static final BigDecimal ERROR_VALUE = BigDecimal.valueOf(-1.0);

    @RequestMapping("/")
    public String stocksInformation() throws InterruptedException {
        try {
            // Read data from input text file
            Stream<String> streams = Files.lines(Paths.get("/Users/tanvi.bhonsle/Downloads/Stocks1.txt"));

            //Create csv for output data
            String csv = "/Users/tanvi.bhonsle/Downloads/output1.csv";

            CSVWriter writer = new CSVWriter(new FileWriter(csv));
            //List that stores the required data from the Yahoo API
            List<StockInformation> stockData = new ArrayList<>();
//            final Integer[] idCounter = {1};

            ExecutorService executor = Executors.newFixedThreadPool(10);
            Collection callableList = new LinkedList<>();
            streams.forEach(stream -> {
                callableList.add(new Callable() {
                    @Override
                    public Boolean call() throws Exception {
                        Stock stock = getStockInformationFromYahoo(stream);
                        StockInformation stockInformation = new StockInformation();
                        stockInformation.setStockCode(stream);
                        if (stock != null) {
                            stockInformation = getRequiredStockData(stock, stockInformation);
                        } else {
                            stockInformation = addErrorDataToStockInformation(stockInformation);
                        }
                        stockData.add(stockInformation);
//                        StockInformation stockInformation = new StockInformation();
////                        stockInformation.setId(idCounter[0]++);
//                        stockInformation.setStockCode(stream);
//                        Stock stock = null;
//                        try {
//                            stock = YahooFinance.get(stream);
//                            logger.debug("finance data fetched for stock code: " + stream);
//                        } catch (IOException e) {
//                            System.out.print("Error while fetching finance data");
//                        }
//                        BigDecimal price = stock.getQuote().getPrice();
//                        stockInformation.setCurrentPrice(price);
//                        BigDecimal yearHigh = stock.getQuote().getYearHigh();
//                        stockInformation.setYearHigh(yearHigh);
//                        BigDecimal yearLow = stock.getQuote().getYearLow();
//                        stockInformation.setYearLow(yearLow);
//                        BigDecimal oneYearTargetPrice = stock.getStats().getOneYearTargetPrice();
//                        stockInformation.setTargetPrice(oneYearTargetPrice);


                        return true;
                    }
                });
            });
            List futures = executor.invokeAll(callableList);

            executor.shutdown();

            BeanToCsv bc = new BeanToCsv();
            ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();

            mappingStrategy.setType(StockInformation.class);

            String[] columns = new String[]{"stockCode","currentPrice","targetPrice", "yearHigh", "yearLow"};

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

    private StockInformation addErrorDataToStockInformation(StockInformation stockInformation) {
        System.out.print("in add ErrorData");
//        StockInformation stockInformation = new StockInformation();
        stockInformation.setCurrentPrice(ERROR_VALUE);
        stockInformation.setTargetPrice(ERROR_VALUE);
        stockInformation.setYearHigh(ERROR_VALUE);
        stockInformation.setYearLow(ERROR_VALUE);
        return stockInformation;
    }

    private StockInformation getRequiredStockData(Stock stock, StockInformation stockInformation) {
        System.out.print("in add getRequiredStockData");
//        StockInformation stockInformation = new StockInformation();
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
    }

    /**
     * @param stream
     * @return
     */
    private Stock getStockInformationFromYahoo(String stream) {
        try {
            System.out.print("in add getStockInfromationfrom Yahoo");
            Stock stock = YahooFinance.get(stream);
            logger.debug("finance data fetched for stock code: " + stream);
            return stock;
        } catch (IOException e) {
            System.out.print("Error while fetching finance data");
            return null;
        }
    }

}
