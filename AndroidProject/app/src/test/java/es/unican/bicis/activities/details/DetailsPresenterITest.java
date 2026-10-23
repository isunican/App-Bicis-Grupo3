package es.unican.bicis.activities.details;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import es.unican.bicis.model.Network;
import es.unican.bicis.repository.JsonNetworksRepository;
import es.unican.bicis.repository.NetworkDetailsCallback;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class DetailsPresenterITest {

    // Texto que muestra el presenter cuando un dato no esta disponible
    private static final String NA = DetailsPresenter.NOT_AVAILABLE;

    private JsonNetworksRepository repository;
    private IDetailsContract.View mockView;
    private DetailsPresenter sut;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        repository = new JsonNetworksRepository(context);
        mockView = mock(IDetailsContract.View.class);
        sut = new DetailsPresenter();
    }

    /**
     * Lee una red del repositorio JSON a partir de su id.
     * El repositorio responde en el momento, asi que la red ya esta
     * disponible cuando termina la llamada.
     */
    private Network loadNetwork(String networkId) {
        final Network[] result = new Network[1];
        repository.requestNetwork(new NetworkDetailsCallback() {
            @Override
            public void onSuccess(Network network) {
                result[0] = network;
            }

            @Override
            public void onFailure(Throwable e) {
                fail("No se pudo cargar la red " + networkId + ": " + e.getMessage());
            }
        }, networkId);
        assertNotNull("Red no cargada: " + networkId, result[0]);
        return result[0];
    }

    /** IDP.6a: Bicing, una red con todos los datos. */
    @Test
    public void init_bicing_showsAllTheData() {
        sut.init(mockView, loadNetwork("bicing"));

        verify(mockView).showName("Bicing");
        verify(mockView).showLocation("Barcelona, ES");
        verify(mockView).showLatitude("41.3851");
        verify(mockView).showLongitude("2.1734");
        verify(mockView).showEbikes(DetailsPresenter.YES);
        verify(mockView).showOperatorsCount("3");
        verify(mockView).showOperators(
                "• Barcelona de Serveis Municipals, S.A. (BSM)\n• CESPA\n• PBSC");
        verify(mockView, never()).showNoDetailsAvailable();
        verifyNoMoreInteractions(mockView);
    }

    /**
     * Complementa IDP.6a: BiciMAD no tiene ebikes en su JSON.
     * Debe mostrarse "-" y no "No" (dato ausente no es lo mismo que false).
     */
    @Test
    public void init_bicimad_ebikesAbsentIsNotShownAsNo() {
        sut.init(mockView, loadNetwork("bicimad"));

        verify(mockView).showName("BiciMAD");
        verify(mockView).showLocation("Madrid, ES");
        verify(mockView).showLatitude("40.4168");
        verify(mockView).showLongitude("-3.7038");
        verify(mockView).showEbikes(NA);
        verify(mockView).showOperatorsCount("1");
        verify(mockView).showOperators("• Empresa Municipal de Transportes de Madrid, S.A.");
        verify(mockView, never()).showNoDetailsAvailable();
        verifyNoMoreInteractions(mockView);
    }

    /**
     * IDP.6b: Bici Norte, una red con datos ausentes
     * (sin longitud, sin ebikes y sin company).
     * Usa res/raw/network_bicinorte.json (Anexo 1 del plan).
     */
    @Test
    public void init_biciNorte_missingDataShownAsNotAvailable() {
        sut.init(mockView, loadNetwork("bicinorte"));

        verify(mockView).showName("Bici Norte");
        verify(mockView).showLocation("Bilbao, ES");
        verify(mockView).showLatitude("43.2630");
        verify(mockView).showLongitude(NA);
        verify(mockView).showEbikes(NA);
        verify(mockView).showOperatorsCount(NA);
        verify(mockView).showOperators(NA);
        verify(mockView, never()).showNoDetailsAvailable();
        verifyNoMoreInteractions(mockView);
    }

    /**
     * IDP.6c: Bici Sur, una red sin datos aparte de nombre, ciudad y pais.
     * Debe mostrar solo el mensaje de "sin informacion".
     * Usa res/raw/network_bicisur.json (Anexo 1 del plan).
     */
    @Test
    public void init_biciSur_showsNoDetailsMessage() {
        sut.init(mockView, loadNetwork("bicisur"));

        verify(mockView).showName("Bici Sur");
        verify(mockView).showLocation("Cádiz, ES");
        verify(mockView).showNoDetailsAvailable();
        verify(mockView, never()).showLatitude(anyString());
        verify(mockView, never()).showLongitude(anyString());
        verify(mockView, never()).showEbikes(anyString());
        verify(mockView, never()).showOperatorsCount(anyString());
        verify(mockView, never()).showOperators(anyString());
        verifyNoMoreInteractions(mockView);
    }

    /** IDP.6d: no llega ninguna red [null]. Sale el mensaje y no hay excepcion. */
    @Test
    public void init_nullNetwork_showsNoDetailsMessage() {
        sut.init(mockView, null);

        verify(mockView).showNoDetailsAvailable();
        verifyNoMoreInteractions(mockView);
    }
}