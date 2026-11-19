package es.unican.bicis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import es.unican.bicis.model.NetworksResponse;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class ExampleTest {

    @Test
    public void test() {
        NetworksResponse response = new NetworksResponse();
        assertEquals(0, response.getNetworksCount());
    }
}