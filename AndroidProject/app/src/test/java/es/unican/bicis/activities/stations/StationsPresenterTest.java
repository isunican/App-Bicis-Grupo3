package es.unican.bicis.activities.stations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import es.unican.bicis.common.StationUtils;
import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;
import es.unican.bicis.repository.INetworksRepository;
import es.unican.bicis.repository.StationsCallback;

/**
 * Unit tests for {@link StationsPresenter}.
 * The view and the repository are replaced by Mockito mocks.
 * Loading of the stations (UPR.2): HU 532377 Listar estaciones.
 * Filter by free bikes and access to the details (UPR.1, UPR.3, UPR.4): HU 532376 Filtrar estaciones.
 * Search by name (UPB): HU 532314 Buscar estación por nombre.
 * Filter by empty slots (UPH.1): HU 532335 Filtrar estaciones por huecos.
 */
public class StationsPresenterTest {

    private IStationsContract.View mockView;
    private INetworksRepository mockRepository;
    private StationsPresenter sut;

    private Network network;
    private Station s0;
    private Station s3;
    private Station s5;
    private Station sNull;
    private List<Station> allStations;

    @Before
    public void setUp() {
        mockView = mock(IStationsContract.View.class);
        mockRepository = mock(INetworksRepository.class);
        when(mockView.getNetworksRepository()).thenReturn(mockRepository);
        sut = new StationsPresenter();

        network = createNetwork("tuebici", "TUeBICI", "Santander", "ES");
        s0 = createStation("Estacion 0", 0);
        s3 = createStation("Estacion 3", 3);
        s5 = createStation("Estacion 5", 5);
        sNull = createStation("Estacion sin dato", null);

        // ordenadas de más a menos bicis libres, que es como las muestra el presenter
        allStations = Arrays.asList(s5, s3, s0, sNull);
    }

    private Network createNetwork(String id, String name, String city, String country) {
        Location location = new Location();
        location.setCity(city);
        location.setCountry(country);

        Network n = new Network();
        n.setId(id);
        n.setName(name);
        n.setLocation(location);
        return n;
    }

    private Station createStation(String name, Integer freeBikes) {
        Station station = new Station();
        station.setName(name);
        station.setFreeBikes(freeBikes);
        return station;
    }

    /** The repository answers the request with the given stations */
    private void repositoryReturns(List<Station> stations) {
        doAnswer(invocation -> {
            StationsCallback cb = invocation.getArgument(0);
            cb.onSuccess(stations);
            return null;
        }).when(mockRepository).requestStations(any(StationsCallback.class), anyString());
    }

    /** The repository fails when requesting the stations */
    private void repositoryFails() {
        doAnswer(invocation -> {
            StationsCallback cb = invocation.getArgument(0);
            cb.onFailure(new Throwable("Sin conexión"));
            return null;
        }).when(mockRepository).requestStations(any(StationsCallback.class), anyString());
    }

    /**
     * Loads the given stations in the presenter and forgets the calls made during
     * the initial load, so that the filter tests only check what happens afterwards.
     */
    private void initPresenterWith(List<Station> stations) {
        repositoryReturns(stations);
        sut.init(mockView, network);
        clearInvocations(mockView);
    }

    // =====================================================================
    // Carga de estaciones (HU 532377 Listar estaciones)
    // =====================================================================

    /** UPR.2a: se muestran la cabecera y las estaciones ordenadas de más a menos bicis. */
    @Test
    @SuppressWarnings("unchecked")
    public void testInitShowsStationsSortedByFreeBikes() {
        Station a = createStation("Alisal", 3);
        Station b = createStation("Correos", 5);
        Station c = createStation("Castilla", null);
        repositoryReturns(Arrays.asList(a, b, c));

        sut.init(mockView, createNetwork("tuebici", "TUeBICI", "Santander", "España"));

        verify(mockRepository).requestStations(any(StationsCallback.class), eq("tuebici"));
        verify(mockView).showNetworkName("TUeBICI");
        verify(mockView).showNetworkLocation("Santander, España");
        verify(mockView).showNetworkLocation("Santander, España · 3 estaciones");

        ArgumentCaptor<List<Station>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).showStations(captor.capture());
        assertEquals(Arrays.asList(b, a, c), captor.getValue());

        verify(mockView, never()).showNoStations();
        verify(mockView, never()).showLoadError();
    }

    /** UPR.2b: se piden las estaciones de cualquier red, no solo de TUeBICI. */
    @Test
    public void testInitAnyNetworkRequestsItsStations() {
        repositoryReturns(Arrays.asList(createStation("Estación", 1)));

        sut.init(mockView, createNetwork("bicing", "Bicing", "Barcelona", "ES"));

        verify(mockRepository).requestStations(any(StationsCallback.class), eq("bicing"));
        verify(mockView).showStations(anyList());
    }

    /** UPR.2c: red sin nombre ni país: se muestra "-" y solo la ciudad. */
    @Test
    public void testInitMissingNameAndCountry() {
        repositoryReturns(Arrays.asList(createStation("Estación", 1)));

        sut.init(mockView, createNetwork("id", " ", "Santander", null));

        verify(mockView).showNetworkName(StationUtils.NO_DATA);
        verify(mockView).showNetworkLocation("Santander");
    }

    /** UPR.2d: red sin ubicación: se muestra "-". */
    @Test
    public void testInitNullLocationShowsDash() {
        Network n = createNetwork("id", "Red", null, null);
        n.setLocation(null);
        repositoryReturns(new ArrayList<>());

        sut.init(mockView, n);

        verify(mockView).showNetworkLocation(StationUtils.NO_DATA);
    }

    /** UPR.2e: red sin estaciones (lista vacía): mensaje de lista vacía. */
    @Test
    public void testInitEmptyListShowsNoStations() {
        repositoryReturns(new ArrayList<>());

        sut.init(mockView, createNetwork("id", "Red", "Ciudad", "ES"));

        verify(mockView).showNoStations();
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showLoadError();
    }

    /** UPR.2f: el repositorio devuelve null: mensaje de lista vacía. */
    @Test
    public void testInitNullListShowsNoStations() {
        repositoryReturns(null);

        sut.init(mockView, createNetwork("id", "Red", "Ciudad", "ES"));

        verify(mockView).showNoStations();
        verify(mockView, never()).showStations(anyList());
    }

    /** UPR.2g: falla la carga de estaciones: mensaje de error y se mantiene la cabecera. */
    @Test
    public void testInitRepositoryFailureShowsLoadError() {
        repositoryFails();

        sut.init(mockView, createNetwork("id", "Red", "Ciudad", "ES"));

        verify(mockView).showNetworkName("Red");
        verify(mockView).showLoadError();
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showNoStations();
    }

    /** UPR.2h: no llega ninguna red: mensaje de error y no se piden estaciones. */
    @Test
    public void testInitNullNetworkShowsLoadError() {
        sut.init(mockView, null);

        verify(mockView).showLoadError();
        verify(mockView, never()).showNetworkName(anyString());
        verify(mockRepository, never()).requestStations(any(StationsCallback.class), anyString());
    }

    /** UPR.2i: red sin id: mensaje de error y no se piden estaciones. */
    @Test
    public void testInitBlankIdShowsLoadErrorWithoutRequest() {
        sut.init(mockView, createNetwork("  ", "Red", "Ciudad", "ES"));

        verify(mockView).showNetworkName("Red");
        verify(mockView).showLoadError();
        verify(mockRepository, never()).requestStations(any(StationsCallback.class), anyString());
    }

    // =====================================================================
    // Filtro por bicis libres (HU 532376 Filtrar estaciones)
    // =====================================================================

    /** UPR.1a: filtro "3": solo las estaciones con 3 o más bicis, en orden. */
    @Test
    public void testFilterShowsOnlyStationsWithAtLeastMinimum() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied("3");

        verify(mockView).showFilterActive(3);
        verify(mockView).showStations(Arrays.asList(s5, s3));
        verify(mockView).showNetworkLocation("Santander, ES · 2 estaciones");
        verify(mockView, never()).showNoStationsWithMinFreeBikes(anyInt());
        verify(mockView, never()).showInvalidMinFreeBikes();
    }

    /** UPR.1b: filtro "5": se incluye la estación con exactamente 5 bicis (valor límite). */
    @Test
    public void testFilterIncludesStationsEqualToMinimum() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied("5");

        verify(mockView).showStations(Arrays.asList(s5));
    }

    /** UPR.1c: filtro "1", el menor valor válido (valor límite). */
    @Test
    public void testFilterMinimumValueOne() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied("1");

        verify(mockView).showStations(Arrays.asList(s5, s3));
    }

    /** UPR.1d: filtro " 5 " con espacios: se acepta. */
    @Test
    public void testFilterAcceptsValueWithSpaces() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied(" 5 ");

        verify(mockView).showStations(Arrays.asList(s5));
    }

    // ---------- Quitar filtros (HU 532317)

    // Con un filtro aplicado, se quita y se ve la lista completa.
    @Test
    public void testQuitarFiltrosTrasUnFiltroMuestraListaCompleta() {
        initPresenterWith(allStations);
        sut.onFilterApplied("3");
        clearInvocations(mockView);

        sut.onFilterCleared();

        verify(mockView).showStations(allStations);
        verify(mockView, never()).mostrarErrorQuitarFiltros();
    }

    // Con varios filtros aplicados, una sola acción los quita todos.
    @Test
    public void testQuitarFiltrosTrasVariosFiltrosMuestraListaCompleta() {
        initPresenterWith(allStations);
        sut.onFilterApplied("1");
        sut.onFilterApplied("3");
        sut.onFilterApplied("5");
        clearInvocations(mockView);

        sut.onFilterCleared();

        verify(mockView).showStations(allStations);
        verify(mockView, never()).mostrarErrorQuitarFiltros();
    }

    // Si falla al quitar los filtros, se muestra el error.
    @Test
    public void testQuitarFiltrosConFalloMuestraError() {
        initPresenterWith(allStations);
        sut.onFilterApplied("3");
        clearInvocations(mockView);
        doThrow(new RuntimeException("fallo simulado")).when(mockView).showStations(allStations);

        sut.onFilterCleared();

        verify(mockView).mostrarErrorQuitarFiltros();
    }

    // Sin filtros aplicados, la lista sigue completa y no hay error.
    @Test
    public void testQuitarFiltrosSinFiltroAplicadoMantieneListaCompleta() {
        initPresenterWith(allStations);

        sut.onFilterCleared();

        verify(mockView).showStations(allStations);
        verify(mockView, never()).mostrarErrorQuitarFiltros();
        verify(mockView, never()).showInvalidMinFreeBikes();
    }

    // ---------- Sin resultados y valores no válidos

    /** UPR.1e: filtro "6", mayor que todas: mensaje de sin resultados. */
    @Test
    public void testFilterWithoutResultsShowsMessage() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied("6");

        verify(mockView).showFilterActive(6);
        verify(mockView).showNoStationsWithMinFreeBikes(6);
        verify(mockView).showNetworkLocation("Santander, ES");
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showInvalidMinFreeBikes();
    }

    /** UPR.1f: red sin estaciones y filtro "1": mensaje de sin resultados. */
    @Test
    public void testFilterWithoutStationsShowsMessage() {
        initPresenterWith(new ArrayList<>());

        sut.onFilterApplied("1");

        verify(mockView).showNoStationsWithMinFreeBikes(1);
    }

    /** UPR.1g: filtro "0": valor no válido (valor límite). */
    @Test
    public void testFilterZeroIsInvalid() {
        assertInvalid("0");
    }

    /** UPR.1h: filtro "-1": valor no válido. */
    @Test
    public void testFilterNegativeIsInvalid() {
        assertInvalid("-1");
    }

    /** UPR.1i: filtro "abc": valor no válido. */
    @Test
    public void testFilterTextIsInvalid() {
        assertInvalid("abc");
    }

    /** UPR.1j: filtro "2.5": valor no válido. */
    @Test
    public void testFilterDecimalIsInvalid() {
        assertInvalid("2.5");
    }

    /** UPR.1k: filtro vacío: valor no válido. */
    @Test
    public void testFilterEmptyIsInvalid() {
        assertInvalid("");
    }

    /** UPR.1l: filtro null: valor no válido. */
    @Test
    public void testFilterNullIsInvalid() {
        assertInvalid(null);
    }

    /** UPR.1m: filtro "99999999999", fuera del rango de int: valor no válido. */
    @Test
    public void testFilterTooBigNumberIsInvalid() {
        assertInvalid("99999999999");
    }

    private void assertInvalid(String input) {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));

        sut.onFilterApplied(input);

        verify(mockView).showInvalidMinFreeBikes();
        verify(mockView, never()).showFilterActive(anyInt());
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showNoStationsWithMinFreeBikes(anyInt());
    }

    /** UPR.1n: las estaciones sin dato de bicis no se muestran al filtrar. */
    @Test
    public void testStationsWithoutDataAreNotShownWhenFiltering() {
        initPresenterWith(Arrays.asList(sNull, s3));

        sut.onFilterApplied("1");

        verify(mockView).showStations(Arrays.asList(s3));
    }

    /** UPR.1o: solo hay estaciones sin dato: mensaje de sin resultados. */
    @Test
    public void testOnlyStationsWithoutDataShowsNoResults() {
        initPresenterWith(Arrays.asList(sNull));

        sut.onFilterApplied("1");

        verify(mockView).showNoStationsWithMinFreeBikes(1);
    }

    /** UPR.3a: al quitar el filtro se vuelven a mostrar todas las estaciones. */
    @Test
    public void testClearFilterShowsAllStations() {
        initPresenterWith(Arrays.asList(s0, s3, s5, sNull));
        sut.onFilterApplied("3");
        clearInvocations(mockView);

        sut.onFilterCleared();

        verify(mockView).showFilterInactive();
        verify(mockView).showStations(Arrays.asList(s5, s3, s0, sNull));
        verify(mockView).showNetworkLocation("Santander, ES · 4 estaciones");
    }

    /** UPR.3b: quitar el filtro tras un error de carga: se mantiene el error. */
    @Test
    public void testClearFilterAfterLoadErrorShowsLoadError() {
        repositoryFails();
        sut.init(mockView, network);
        sut.onFilterApplied("1");
        clearInvocations(mockView);

        sut.onFilterCleared();

        verify(mockView).showFilterInactive();
        verify(mockView).showLoadError();
        verify(mockView, never()).showStations(anyList());
    }

    // =====================================================================
    // Acceso al detalle de la red
    // =====================================================================

    /** UPR.4a: botón de información: se abre el detalle de la red. */
    @Test
    public void testInfoClickedOpensNetworkDetails() {
        initPresenterWith(Arrays.asList(s3));

        sut.onInfoClicked();

        verify(mockView).showNetworkDetails(network);
    }

    /** UPR.4b: botón de información sin red: no se abre nada. */
    @Test
    public void testInfoClickedWithoutNetworkDoesNothing() {
        sut.init(mockView, null);

        sut.onInfoClicked();

        verify(mockView, never()).showNetworkDetails(any());
    }

    // =====================================================================
    // Busqueda de estaciones por nombre (HU 532314)
    // Tabla 5 del plan de pruebas
    // =====================================================================

    /**
     * UPB.1a: buscar "Plaza" muestra las dos estaciones coincidentes.
     * No debe mostrarse el mensaje de estacion inexistente.
     */
    @Test
    public void testSearchByNameShowsMatchingStations() {
        Station plazaItalia = createStation("Plaza de Italia", 5);
        Station magdalena = createStation("Magdalena", 3);
        Station plazaPombo = createStation("Plaza Pombo (NE)", 1);

        initPresenterWith(Arrays.asList(
                plazaItalia, magdalena, plazaPombo));

        sut.onSearchTextChanged("Plaza");

        verify(mockView).showStations(Arrays.asList(
                plazaItalia, plazaPombo));
        verify(mockView, never()).showStationNotFound();
    }

    /**
     * UPB.1b: una busqueda vacia recupera todas las estaciones
     * en el mismo orden original.
     */
    @Test
    public void testSearchEmptyTextShowsAllStations() {
        Station plazaItalia = createStation("Plaza de Italia", 5);
        Station magdalena = createStation("Magdalena", 3);
        Station plazaPombo = createStation("Plaza Pombo (NE)", 1);

        List<Station> estaciones = Arrays.asList(
                plazaItalia, magdalena, plazaPombo);

        initPresenterWith(estaciones);

        sut.onSearchTextChanged("");

        verify(mockView).showStations(estaciones);
        verify(mockView, never()).showStationNotFound();
    }

    /**
     * UPB.1c: buscar "Magdalna" muestra el mensaje de estacion
     * inexistente y sugiere Magdalena.
     */
    @Test
    public void testSearchMisspelledNameShowsSuggestion() {
        Station plazaItalia = createStation("Plaza de Italia", 5);
        Station magdalena = createStation("Magdalena", 3);
        Station plazaPombo = createStation("Plaza Pombo (NE)", 1);

        initPresenterWith(Arrays.asList(
                plazaItalia, magdalena, plazaPombo));

        sut.onSearchTextChanged("Magdalna");

        verify(mockView).showStationNotFound();
        verify(mockView).showSuggestion(magdalena);
        verify(mockView, never()).showStations(anyList());
    }

    /**
     * UPB.1d: buscar "Zzzz" muestra el mensaje de estacion
     * inexistente sin sugerir ninguna estacion.
     */
    @Test
    public void testSearchUnknownNameWithoutSuggestion() {
        Station plazaItalia = createStation("Plaza de Italia", 5);
        Station magdalena = createStation("Magdalena", 3);
        Station plazaPombo = createStation("Plaza Pombo (NE)", 1);

        initPresenterWith(Arrays.asList(
                plazaItalia, magdalena, plazaPombo));

        sut.onSearchTextChanged("Zzzz");

        verify(mockView).showStationNotFound();
        verify(mockView, never()).showSuggestion(any(Station.class));
        verify(mockView, never()).showStations(anyList());
    }

    /**
     * UPB.2a: seleccionar Magdalena solicita mostrar
     * la ubicacion de esa estacion.
     */
    @Test
    public void testSelectingStationShowsItsLocation() {
        Station plazaItalia = createStation("Plaza de Italia", 5);
        Station magdalena = createStation("Magdalena", 3);
        Station plazaPombo = createStation("Plaza Pombo (NE)", 1);

        initPresenterWith(Arrays.asList(
                plazaItalia, magdalena, plazaPombo));

        sut.onStationSelected(magdalena);

        verify(mockView).showStationLocation(magdalena);
    }

    /**
     * UPB.2b: seleccionar una estacion null no solicita
     * mostrar ninguna ubicacion ni lanza excepciones.
     */
    @Test
    public void testSelectingNullStationDoesNothing() {
        Station plazaItalia = createStation("Plaza de Italia", 5);
        Station magdalena = createStation("Magdalena", 3);
        Station plazaPombo = createStation("Plaza Pombo (NE)", 1);

        initPresenterWith(Arrays.asList(
                plazaItalia, magdalena, plazaPombo));

        sut.onStationSelected(null);

        verify(mockView, never()).showStationLocation(any(Station.class));
    }

    // =====================================================================
    // Filtro por huecos libres (HU 532335 Filtrar estaciones por huecos)
    // Pruebas unitarias UPH.1 del plan de pruebas (Tabla 5).
    // Estaciones con 0, 4, 7 y sin dato de huecos libres. Sus bicis (3, 2, 1, 0)
    // hacen que el orden habitual de la lista (por bicis) sea h0, h4, h7, hNull,
    // distinto del orden por huecos (h7, h4), para comprobar la ordenación.
    // =====================================================================

    private Station h0;
    private Station h4;
    private Station h7;
    private Station hNull;

    /** Crea una estación con el número de bicis y de huecos libres indicados. */
    private Station createStation(String name, Integer freeBikes, Integer emptySlots) {
        Station station = createStation(name, freeBikes);
        station.setEmptySlots(emptySlots);
        return station;
    }

    /** Carga en el presenter las estaciones con 0, 4, 7 y sin dato de huecos libres. */
    private void initPresenterWithEmptySlots() {
        h0 = createStation("Huecos 0", 3, 0);
        h4 = createStation("Huecos 4", 2, 4);
        h7 = createStation("Huecos 7", 1, 7);
        hNull = createStation("Huecos sin dato", 0, null);
        initPresenterWith(Arrays.asList(h0, h4, h7, hNull));
    }

    /** UPH.1a: mínimo "4": se muestran [7, 4], de más a menos huecos, y el embudo indica 1 filtro. */
    @Test
    public void testEmptySlotsFilterShowsStationsSortedByEmptySlots() {
        initPresenterWithEmptySlots();

        sut.onMinEmptySlotsFilterApplied("4");

        verify(mockView).showMinEmptySlotsFilterActive(4);
        verify(mockView).showActiveFiltersCount(1);
        verify(mockView).showStations(Arrays.asList(h7, h4));
        verify(mockView).showNetworkLocation("Santander, ES · 2 estaciones");
        verify(mockView, never()).showInvalidMinEmptySlots();
    }

    /** UPH.1b: mínimo "7" (valor límite): se incluye la estación con exactamente 7 huecos. */
    @Test
    public void testEmptySlotsFilterIncludesStationsEqualToMinimum() {
        initPresenterWithEmptySlots();

        sut.onMinEmptySlotsFilterApplied("7");

        verify(mockView).showStations(Arrays.asList(h7));
    }

    /** UPH.1c: mínimo "1" (menor valor que filtra): se muestran [7, 4]. */
    @Test
    public void testEmptySlotsFilterMinimumValueOne() {
        initPresenterWithEmptySlots();

        sut.onMinEmptySlotsFilterApplied("1");

        verify(mockView).showStations(Arrays.asList(h7, h4));
    }

    /** UPH.1d: " 7 " (con espacios): se acepta como 7. */
    @Test
    public void testEmptySlotsFilterAcceptsValueWithSpaces() {
        initPresenterWithEmptySlots();

        sut.onMinEmptySlotsFilterApplied(" 7 ");

        verify(mockView).showStations(Arrays.asList(h7));
    }

    /** UPH.1e: mínimo "8" (justo por encima del máximo): mensaje de sin resultados. */
    @Test
    public void testEmptySlotsFilterWithoutResultsShowsMessage() {
        initPresenterWithEmptySlots();

        sut.onMinEmptySlotsFilterApplied("8");

        verify(mockView).showNoStationsWithMinEmptySlots(8);
        verify(mockView).showNetworkLocation("Santander, ES");
        verify(mockView, never()).showStations(anyList());
    }

    /**
     * UPH.1f: mínimo "0": se muestran las 4 estaciones, también la que no tiene dato,
     * en el orden habitual de la lista, y el embudo no indica ningún filtro.
     */
    @Test
    public void testEmptySlotsFilterZeroShowsAllStations() {
        initPresenterWithEmptySlots();

        sut.onMinEmptySlotsFilterApplied("0");

        verify(mockView).showStations(Arrays.asList(h0, h4, h7, hNull));
        verify(mockView).showActiveFiltersCount(0);
        verify(mockView, never()).showInvalidMinEmptySlots();
        verify(mockView, never()).showNoStationsWithMinEmptySlots(anyInt());
    }

    /**
     * UPH.1g: valores no válidos ("-1", "abc", "2.5", "", null y un número demasiado grande):
     * se muestra el error y no se filtra ni cambia la lista.
     */
    @Test
    public void testEmptySlotsFilterInvalidValues() {
        String[] invalidValues = {"-1", "abc", "2.5", "", null, "99999999999"};
        for (String value : invalidValues) {
            initPresenterWithEmptySlots();

            sut.onMinEmptySlotsFilterApplied(value);

            verify(mockView).showInvalidMinEmptySlots();
            verify(mockView, never()).showMinEmptySlotsFilterActive(anyInt());
            verify(mockView, never()).showActiveFiltersCount(anyInt());
            verify(mockView, never()).showStations(anyList());
            verify(mockView, never()).showNoStationsWithMinEmptySlots(anyInt());
        }
    }

    /** UPH.1h: mínimo "1" y solo estaciones sin dato de huecos: mensaje de sin resultados. */
    @Test
    public void testEmptySlotsFilterOnlyStationsWithoutData() {
        initPresenterWith(Arrays.asList(createStation("Huecos sin dato", 0, null)));

        sut.onMinEmptySlotsFilterApplied("1");

        verify(mockView).showNoStationsWithMinEmptySlots(1);
        verify(mockView, never()).showStations(anyList());
    }

    /**
     * UPH.1i: mínimo "4" con el filtro de bicis activo (mínimo 1): solo se muestra la estación
     * que cumple los dos filtros y el embudo indica 2 filtros activos.
     */
    @Test
    public void testEmptySlotsFilterCombinedWithFreeBikesFilter() {
        Station noBikes = createStation("0 bicis y 7 huecos", 0, 7);
        Station both = createStation("2 bicis y 4 huecos", 2, 4);
        initPresenterWith(Arrays.asList(noBikes, both));
        sut.onFilterApplied("1");
        clearInvocations(mockView);

        sut.onMinEmptySlotsFilterApplied("4");

        verify(mockView).showActiveFiltersCount(2);
        verify(mockView).showStations(Arrays.asList(both));
    }

    // =====================================================================
    // Filtro por distancia (HU 532304 Filtrar estaciones por distancia)
    // Pruebas unitarias UPD del plan de pruebas.
    //
    // Coordenadas de referencia (Santander):
    //   usuario  → 43.4620, -3.8100
    //   cerca    → 43.4630, -3.8120  (~0.17 km)
    //   lejos    → 43.5200, -3.9000  (~7.6  km)
    //   sinCoords → sin latitud/longitud
    // =====================================================================

    /** Latitud/longitud del usuario simulado (Santander centro). */
    private static final double USER_LAT = 43.4620;
    private static final double USER_LON = -3.8100;

    /** Ubicación del usuario como array {lat, lon}, devuelta por getUserLocation(). */
    private static final double[] USER_LOCATION = {USER_LAT, USER_LON};

    /** Crea una estación con coordenadas geográficas. */
    private Station createStationWithCoords(String name, Integer freeBikes,
                                            Double lat, Double lon) {
        Station station = createStation(name, freeBikes);
        station.setLatitude(lat);
        station.setLongitude(lon);
        return station;
    }

    /**
     * Inicializa el presenter con estaciones que tienen distintas distancias al usuario
     * y prepara el mock de getUserLocation() para devolver la ubicación del usuario.
     */
    private Station dCerca;
    private Station dLejos;
    private Station dSinCoords;

    private void initPresenterWithDistanceStations() {
        dCerca    = createStationWithCoords("Cerca",     3, 43.4630, -3.8120);  // ~0.17 km
        dLejos    = createStationWithCoords("Lejos",     1, 43.5200, -3.9000);  // ~7.6  km
        dSinCoords = createStation("Sin coordenadas",    2);                     // sin coords
        initPresenterWith(Arrays.asList(dCerca, dSinCoords, dLejos));
        when(mockView.getUserLocation()).thenReturn(USER_LOCATION);
    }

    /**
     * UPD.1a: distancia "1" km → la estación cercana más la sin coordenadas al final
     * (el modelo las incluye siempre que haya al menos una dentro del radio).
     * El presenter muestra las estaciones con distancia, el filtro activo y el contador.
     */
    @Test
    public void testDistanceFilterShowsNearbyStations() {
        initPresenterWithDistanceStations();

        sut.onDistanceFilterApplied("1");

        verify(mockView).showDistanceFilterActive(1.0);
        verify(mockView).showActiveFiltersCount(1);
        // dCerca (dentro del radio) + dSinCoords (sin coords, va al final)
        verify(mockView).showStationsWithDistance(eq(Arrays.asList(dCerca, dSinCoords)), any());
        verify(mockView).showNetworkLocation("Santander, ES · 2 estaciones");
        verify(mockView, never()).showNoStationsWithinDistance(anyDouble());
        verify(mockView, never()).showInvalidMaxDistance();
    }

    /**
     * UPD.1b: distancia "10" km → se incluyen la estación cercana y la lejana
     * (ordenadas de más cercana a más lejana); la sin coordenadas se añade al final.
     */
    @Test
    public void testDistanceFilterLargeRadioIncludesAll() {
        initPresenterWithDistanceStations();

        sut.onDistanceFilterApplied("10");

        verify(mockView).showActiveFiltersCount(1);
        ArgumentCaptor<List<Station>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).showStationsWithDistance(captor.capture(), any());
        List<Station> shown = captor.getValue();
        // La primera debe ser la más cercana; la sin coordenadas va al final
        assertEquals(dCerca, shown.get(0));
        assertEquals(dSinCoords, shown.get(shown.size() - 1));
    }

    /**
     * UPD.1c: distancia 0.5 km (valor límite holgado): la estación cercana se incluye,
     * la lejana no; la sin coordenadas aparece al final por comportamiento del modelo.
     */
    @Test
    public void testDistanceFilterBoundaryStationIsIncluded() {
        initPresenterWithDistanceStations();
        // dCerca está a ~0.17 km → dentro; dLejos a ~7.6 km → fuera
        sut.onDistanceFilterApplied("0.5");

        ArgumentCaptor<List<Station>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).showStationsWithDistance(captor.capture(), any());
        assertTrue(captor.getValue().contains(dCerca));
        assertFalse(captor.getValue().contains(dLejos));
        // dSinCoords se añade al final porque hay resultados dentro del radio
        assertTrue(captor.getValue().contains(dSinCoords));
    }

    /**
     * UPD.1d: distancia muy pequeña ("0.001" km): ninguna estación la cumple →
     * se muestra el mensaje de sin resultados.
     */
    @Test
    public void testDistanceFilterNoResultsShowsMessage() {
        initPresenterWithDistanceStations();

        sut.onDistanceFilterApplied("0.001");

        verify(mockView).showDistanceFilterActive(0.001);
        verify(mockView).showNoStationsWithinDistance(0.001);
        verify(mockView).showNetworkLocation("Santander, ES");
        verify(mockView, never()).showStationsWithDistance(anyList(), any());
    }

    /**
     * UPD.1e: sin estaciones en la red y distancia "5" km → mensaje de sin resultados.
     */
    @Test
    public void testDistanceFilterEmptyNetworkShowsNoResults() {
        initPresenterWith(new ArrayList<>());
        when(mockView.getUserLocation()).thenReturn(USER_LOCATION);

        sut.onDistanceFilterApplied("5");

        verify(mockView).showNoStationsWithinDistance(5.0);
        verify(mockView, never()).showStationsWithDistance(anyList(), any());
    }

    /**
     * UPD.1f: la ubicación del usuario no está disponible (getUserLocation devuelve null):
     * se muestra el mensaje de ubicación no disponible y no se filtra.
     */
    @Test
    public void testDistanceFilterLocationUnavailableShowsMessage() {
        initPresenterWithDistanceStations();
        when(mockView.getUserLocation()).thenReturn(null);

        sut.onDistanceFilterApplied("2");

        verify(mockView).showLocationUnavailableForFilter();
        verify(mockView, never()).showDistanceFilterActive(anyDouble());
        verify(mockView, never()).showStationsWithDistance(anyList(), any());
        verify(mockView, never()).showNoStationsWithinDistance(anyDouble());
    }

    /**
     * UPD.1g: valores no válidos ("0", "-1", "abc", "", null) →
     * se muestra el mensaje de distancia no válida y no se filtra.
     */
    @Test
    public void testDistanceFilterInvalidValuesShowError() {
        String[] invalidValues = {"0", "-1", "abc", "", null};
        for (String value : invalidValues) {
            initPresenterWithDistanceStations();

            sut.onDistanceFilterApplied(value);

            verify(mockView).showInvalidMaxDistance();
            verify(mockView, never()).showDistanceFilterActive(anyDouble());
            verify(mockView, never()).showActiveFiltersCount(anyInt());
            verify(mockView, never()).showStationsWithDistance(anyList(), any());
            verify(mockView, never()).showNoStationsWithinDistance(anyDouble());
        }
    }

    /**
     * UPD.1h: distancia con coma decimal ("1,5" km): se acepta igual que "1.5".
     */
    @Test
    public void testDistanceFilterAcceptsCommaAsDecimalSeparator() {
        initPresenterWithDistanceStations();

        sut.onDistanceFilterApplied("1,5");

        verify(mockView).showDistanceFilterActive(1.5);
        verify(mockView, never()).showInvalidMaxDistance();
    }

    /**
     * UPD.1i: distancia " 1 " con espacios: se acepta como 1.0 km.
     */
    @Test
    public void testDistanceFilterAcceptsValueWithSpaces() {
        initPresenterWithDistanceStations();

        sut.onDistanceFilterApplied(" 1 ");

        verify(mockView).showDistanceFilterActive(1.0);
        verify(mockView, never()).showInvalidMaxDistance();
    }

    /**
     * UPD.1j: filtro de distancia activo + filtro de bicis activo (mínimo 1 bici):
     * solo se muestran las estaciones que cumplen los dos filtros y el embudo indica 2.
     */
    @Test
    public void testDistanceFilterCombinedWithFreeBikesFilter() {
        Station cercaConBicis = createStationWithCoords("Cerca con bicis", 3, 43.4630, -3.8120);
        Station cercaSinBicis = createStationWithCoords("Cerca sin bicis", 0, 43.4635, -3.8125);
        initPresenterWith(Arrays.asList(cercaConBicis, cercaSinBicis));
        when(mockView.getUserLocation()).thenReturn(USER_LOCATION);
        sut.onFilterApplied("1");
        clearInvocations(mockView);

        sut.onDistanceFilterApplied("1");

        verify(mockView).showActiveFiltersCount(2);
        ArgumentCaptor<List<Station>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockView).showStationsWithDistance(captor.capture(), any());
        assertEquals(Arrays.asList(cercaConBicis), captor.getValue());
    }

    /**
     * UPD.1k: al quitar los filtros tras el filtro de distancia, se restaura
     * la lista completa y el filtro de distancia queda inactivo.
     */
    @Test
    public void testClearFilterAfterDistanceFilterRestoresFullList() {
        initPresenterWithDistanceStations();
        sut.onDistanceFilterApplied("1");
        clearInvocations(mockView);

        sut.onFilterCleared();

        verify(mockView).showFilterInactive();
        verify(mockView).showActiveFiltersCount(0);
        // Debe llamar a showStations (no a showStationsWithDistance) con la lista completa
        verify(mockView).showStations(anyList());
        verify(mockView, never()).showStationsWithDistance(anyList(), any());
    }
}