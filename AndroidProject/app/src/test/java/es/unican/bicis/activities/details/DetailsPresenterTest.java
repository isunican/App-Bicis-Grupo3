package es.unican.bicis.activities.details;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;

/**
 * Unit tests for {@link DetailsPresenter}.
 * The view is replaced by a Mockito mock, so no Android dependencies are needed.
 */
public class DetailsPresenterTest {

    private IDetailsContract.View mockView;
    private DetailsPresenter sut;

    @Before
    public void setUp() {
        mockView = mock(IDetailsContract.View.class);
        sut = new DetailsPresenter();
    }

    /**
     * Builds a network with the given data. Any parameter can be null to simulate missing data.
     */
    private Network createNetwork(String id, String name, String city, String country,
                                  Double latitude, Double longitude,
                                  Boolean ebikes, String[] company) {
        Location location = new Location();
        location.setCity(city);
        location.setCountry(country);
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        Network network = new Network();
        network.setId(id);
        network.setName(name);
        network.setLocation(location);
        network.setEbikes(ebikes);
        network.setCompany(company);
        return network;
    }

    // ---------- Prueba 1: caso de éxito ----------

    @Test
    public void testInitAllDataCorrect() {
        Network network = createNetwork("bici-santander", "BiciSantander", "Santander", "ES",
                43.4623, -3.8099, true, new String[]{"Empresa A", "Empresa B"});

        sut.init(mockView, network);

        verify(mockView).showName("BiciSantander");
        verify(mockView).showLocation("Santander, ES");
        verify(mockView).showLatitude("43.4623");
        verify(mockView).showLongitude("-3.8099");
        verify(mockView).showEbikes(DetailsPresenter.YES);
        verify(mockView).showOperatorsCount("2");
        verify(mockView).showOperators("• Empresa A\n• Empresa B");
        verify(mockView, never()).showNoDetailsAvailable();
    }

    @Test
    public void testInitEbikesFalseShowsNo() {
        Network network = createNetwork("bici-santander", "BiciSantander", "Santander", "ES",
                43.4623, -3.8099, false, new String[]{"Empresa A"});

        sut.init(mockView, network);

        verify(mockView).showEbikes(DetailsPresenter.NO);
        verify(mockView).showOperatorsCount("1");
    }

    // ---------- Prueba 2: datos ausentes ----------

    @Test
    public void testInitMissingDataShowsDash() {
        // As in the mock-up 2: only latitude available
        Network network = createNetwork("bici-norte", "BiciNorte", "Bilbao", "ES",
                43.4623, null, null, null);

        sut.init(mockView, network);

        verify(mockView).showName("BiciNorte");
        verify(mockView).showLocation("Bilbao, ES");
        verify(mockView).showLatitude("43.4623");
        verify(mockView).showLongitude(DetailsPresenter.NOT_AVAILABLE);
        verify(mockView).showEbikes(DetailsPresenter.NOT_AVAILABLE);
        verify(mockView).showOperatorsCount(DetailsPresenter.NOT_AVAILABLE);
        verify(mockView).showOperators(DetailsPresenter.NOT_AVAILABLE);
        verify(mockView, never()).showNoDetailsAvailable();
    }

    @Test
    public void testInitMissingCountryShowsOnlyCity() {
        Network network = createNetwork("bici-norte", "BiciNorte", "Bilbao", null,
                43.4623, -2.9350, true, new String[]{"Empresa A"});

        sut.init(mockView, network);

        verify(mockView).showLocation("Bilbao");
    }

    @Test
    public void testInitMissingNameShowsDash() {
        Network network = createNetwork("bici-norte", "   ", "Bilbao", "ES",
                43.4623, -2.9350, true, new String[]{"Empresa A"});

        sut.init(mockView, network);

        verify(mockView).showName(DetailsPresenter.NOT_AVAILABLE);
    }

    // ---------- Prueba 3: datos anómalos (coordenadas) ----------

    @Test
    public void testInitCoordinatesOnValidLimits() {
        Network network = createNetwork("id", "Red", "Ciudad", "ES",
                90.0, -180.0, true, new String[]{"Empresa A"});

        sut.init(mockView, network);

        verify(mockView).showLatitude("90.0");
        verify(mockView).showLongitude("-180.0");
    }

    @Test
    public void testInitCoordinatesOnOtherValidLimits() {
        Network network = createNetwork("id", "Red", "Ciudad", "ES",
                -90.0, 180.0, true, new String[]{"Empresa A"});

        sut.init(mockView, network);

        verify(mockView).showLatitude("-90.0");
        verify(mockView).showLongitude("180.0");
    }

    @Test
    public void testInitCoordinatesOutOfRangeShowDash() {
        Network network = createNetwork("id", "Red", "Ciudad", "ES",
                90.0001, -180.0001, true, new String[]{"Empresa A"});

        sut.init(mockView, network);

        verify(mockView).showLatitude(DetailsPresenter.NOT_AVAILABLE);
        verify(mockView).showLongitude(DetailsPresenter.NOT_AVAILABLE);
    }

    @Test
    public void testInitCoordinatesOutOfRangeOtherSideShowDash() {
        Network network = createNetwork("id", "Red", "Ciudad", "ES",
                -90.0001, 180.0001, true, new String[]{"Empresa A"});

        sut.init(mockView, network);

        verify(mockView).showLatitude(DetailsPresenter.NOT_AVAILABLE);
        verify(mockView).showLongitude(DetailsPresenter.NOT_AVAILABLE);
    }

    @Test
    public void testInitOnlyOneCoordinateInvalid() {
        Network network = createNetwork("id", "Red", "Ciudad", "ES",
                43.4623, 200.0, true, new String[]{"Empresa A"});

        sut.init(mockView, network);

        verify(mockView).showLatitude("43.4623");
        verify(mockView).showLongitude(DetailsPresenter.NOT_AVAILABLE);
    }

    // ---------- Prueba 3: datos anómalos (empresas) ----------

    @Test
    public void testInitCompanyWithBlankElementShowsDash() {
        Network network = createNetwork("id", "Red", "Ciudad", "ES",
                43.4623, -3.8099, true, new String[]{"Empresa A", ""});

        sut.init(mockView, network);

        verify(mockView).showOperatorsCount(DetailsPresenter.NOT_AVAILABLE);
        verify(mockView).showOperators(DetailsPresenter.NOT_AVAILABLE);
    }

    @Test
    public void testInitEmptyCompanyListShowsDash() {
        Network network = createNetwork("id", "Red", "Ciudad", "ES",
                43.4623, -3.8099, true, new String[]{});

        sut.init(mockView, network);

        verify(mockView).showOperatorsCount(DetailsPresenter.NOT_AVAILABLE);
        verify(mockView).showOperators(DetailsPresenter.NOT_AVAILABLE);
    }

    // ---------- Prueba 4: red sin información detallada ----------

    @Test
    public void testInitOnlyBasicInfoShowsNoDetailsMessage() {
        // As in the mock-up 3: only name and city
        Network network = createNetwork(null, "BiciSur", "Cádiz", null,
                null, null, null, null);

        sut.init(mockView, network);

        verify(mockView).showName("BiciSur");
        verify(mockView).showLocation("Cádiz");
        verify(mockView).showNoDetailsAvailable();
        verify(mockView, never()).showLatitude(anyString());
        verify(mockView, never()).showLongitude(anyString());
        verify(mockView, never()).showEbikes(anyString());
        verify(mockView, never()).showOperatorsCount(anyString());
        verify(mockView, never()).showOperators(anyString());
    }

    @Test
    public void testInitNullNetworkShowsNoDetailsMessage() {
        sut.init(mockView, null);

        verify(mockView).showNoDetailsAvailable();
        verify(mockView, never()).showName(anyString());
    }

}
