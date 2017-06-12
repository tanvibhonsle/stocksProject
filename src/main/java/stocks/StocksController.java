package stocks;

/**
 * Created by tanvi.bhonsle on 12/06/17.
 */

import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanToCsv;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import org.slf4j.*;
import org.springframework.ui.Model;
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
import java.util.List;
import java.util.stream.Stream;

@RestController
public class StocksController {

    private static final Logger logger = LoggerFactory.getLogger(StocksController.class);

    @RequestMapping("/")
    public String stocksInformation(Model model) {
        try {
            Stream<String> streams = Files.lines(Paths.get("/Users/tanvi.bhonsle/Downloads/Stocks.txt"));

            String csv = "/Users/tanvi.bhonsle/Downloads/output.csv";
            CSVWriter writer = new CSVWriter(new FileWriter(csv));

            List<StockInformation> stockData = new ArrayList<>();

            final Integer[] i = {1};
            streams.forEach(stream -> {
                StockInformation stockInformation = new StockInformation();
                stockInformation.setId(i[0]++);
                stockInformation.setStockCode(stream);
                Stock stock = null;
                try {
                    stock = YahooFinance.get(stream);
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
            });

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
        }
    }

}
