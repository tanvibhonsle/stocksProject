package stocks;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by tanvi.bhonsle on 12/06/17.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(StocksController.class)
public class StocksControllerTest extends TestCase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void TestStocksController() throws Exception{
        this.mockMvc.perform(get("/").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk()).andExpect(content().string("CSV File written successfully!!!"));

    }

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testStocksInformation() throws Exception {

    }
}