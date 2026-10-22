package es.unican.bicis.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.fail;

import es.unican.bicis.R;
import es.unican.bicis.common.Utils;
import es.unican.bicis.repository.JsonNetworksRepository;
import es.unican.bicis.repository.StationsCallback;

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

    @Test
    public void requestStationsBicing() {
        JsonNetworksRepository repo = new JsonNetworksRepository(context);
        repo.requestStations(new StationsCallback() {
            @Override
            public void onSuccess(List<Station> stations) {
                assertEquals(545, stations.size());
            }

            @Override
            public void onFailure(Throwable e) {
                fail("No debería fallar");
            }
        }, "bicing");
    }

    @Test
    public void requestStationsRedInexistenteFalla() {
        JsonNetworksRepository repo = new JsonNetworksRepository(context);
        repo.requestStations(new StationsCallback() {
            @Override
            public void onSuccess(List<Station> stations) {
                fail("Debería fallar");
            }

            @Override
            public void onFailure(Throwable e) {
                assertNotNull(e);
            }
        }, "no-existe");
    }
}
