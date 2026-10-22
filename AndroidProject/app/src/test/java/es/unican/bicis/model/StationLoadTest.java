package es.unican.bicis.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import es.unican.bicis.R;
import es.unican.bicis.common.Utils;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class StationLoadTest {

    private final Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void cargaEstacionesBicing() {
        Network red = Utils.parseNetwork(context, R.raw.network_bicing);

        assertNotNull(red.getStations());
        assertEquals(545, red.getStations().size());

        Station primera = red.getStations().get(0);
        assertEquals(" PL  JOANIC - C / BRUNIQUER, 59", primera.getName());
        assertEquals(Integer.valueOf(0), primera.getFreeBikes());
        assertEquals(Integer.valueOf(13), primera.getEmptySlots());
        assertEquals(Integer.valueOf(0), primera.getEbikes());
    }

    @Test
    public void redSinBicisElectricasDevuelveNull() {
        Network red = Utils.parseNetwork(context, R.raw.network_bicimad);

        assertNull(red.getStations().get(0).getEbikes());
    }
}
