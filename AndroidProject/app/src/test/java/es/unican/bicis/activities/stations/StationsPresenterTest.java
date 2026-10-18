package es.unican.bicis.activities.stations;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.unican.bicis.model.Station;

/**
 * Unit tests for {@link StationsPresenter}.
 * The view is replaced by a Mockito mock, so no Android dependencies are needed.
 */
public class StationsPresenterTest {

    private IStationsContract.View mockView;
    private StationsPresenter sut;

    private Station s0;
    private Station s3;
    private Station s5;
    private Station sNull;
    private List<Station> allStations;

    @Before
    public void setUp() {
        mockView = mock(IStationsContract.View.class);
        sut = new StationsPresenter();

        s0 = createStation("Estacion 0", 0);
        s3 = createStation("Estacion 3", 3);
        s5 = createStation("Estacion 5", 5);
        sNull = createStation("Estacion sin dato", null);
        allStations = Arrays.asList(s0, s3, s5, sNull);
    }

    private Station createStation(String name, Integer freeBikes) {
        Station station = new Station();
        station.setName(name);
        station.setFreeBikes(freeBikes);
        return station;
    }

    /**
     * Initializes the presenter with the given stations and forgets the calls
     * made during the initial load, so that the tests only check the filter.
     */
    private void initPresenterWith(List<Station> stations) {
        sut.init(mockView, stations);
        clearInvocations(mockView);
    }

    // ---------- Carga inicial ----------

    @Test
    public void testInitShowsAllStations() {
        sut.init(mockView, allStations);

        verify(mockView).showStations(allStations);
    }

    @Test
    public void testInitWithNullListShowsEmptyList() {
        sut.init(mockView, null);

        verify(mockView).showStations(new ArrayList<>());
    }

    // ---------- Éxito ----------

    @Test
    public void testFilterShowsOnlyStationsWithAtLeastMinimum() {
        initPresenterWith(allStations);

        sut.onFilterApplied("3");

        verify(mockView).showStations(Arrays.asList(s3, s5));
        verify(mockView, never()).showNoStationsWithMinFreeBikes(anyInt());
        verify(mockView, never()).showInvalidMinFreeBikes();
    }

    @Test
    public void testFilterIncludesStationsEqualToMinimum() {
        initPresenterWith(allStations);

        sut.onFilterApplied("5");

        verify(mockView).showStations(Arrays.asList(s5));
    }

    @Test
    public void testFilterMinimumValueOne() {
        initPresenterWith(allStations);

        sut.onFilterApplied("1");

        verify(mockView).showStations(Arrays.asList(s3, s5));
    }

    @Test
    public void testFilterAcceptsValueWithSpaces() {
        initPresenterWith(allStations);

        sut.onFilterApplied(" 5 ");

        verify(mockView).showStations(Arrays.asList(s5));
    }

    @Test
    public void testClearFilterShowsAllStations() {
        initPresenterWith(allStations);
        sut.onFilterApplied("3");

        sut.onFilterCleared();

        verify(mockView).showStations(allStations);
    }

    // ---------- Sin resultados ----------

    @Test
    public void testFilterWithoutResultsShowsMessage() {
        initPresenterWith(allStations);

        sut.onFilterApplied("6");

        verify(mockView).showNoStationsWithMinFreeBikes(6);
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showInvalidMinFreeBikes();
    }

    @Test
    public void testFilterWithoutStationsShowsMessage() {
        initPresenterWith(new ArrayList<>());

        sut.onFilterApplied("1");

        verify(mockView).showNoStationsWithMinFreeBikes(1);
    }

    // ---------- Valor no válido ----------

    @Test
    public void testFilterZeroIsInvalid() {
        assertInvalid("0");
    }

    @Test
    public void testFilterNegativeIsInvalid() {
        assertInvalid("-1");
    }

    @Test
    public void testFilterTextIsInvalid() {
        assertInvalid("abc");
    }

    @Test
    public void testFilterDecimalIsInvalid() {
        assertInvalid("2.5");
    }

    @Test
    public void testFilterEmptyIsInvalid() {
        assertInvalid("");
    }

    @Test
    public void testFilterNullIsInvalid() {
        assertInvalid(null);
    }

    @Test
    public void testFilterTooBigNumberIsInvalid() {
        assertInvalid("99999999999");
    }

    private void assertInvalid(String input) {
        initPresenterWith(allStations);

        sut.onFilterApplied(input);

        verify(mockView).showInvalidMinFreeBikes();
        verify(mockView, never()).showStations(anyList());
        verify(mockView, never()).showNoStationsWithMinFreeBikes(anyInt());
    }

    // ---------- Estaciones sin dato de bicis ----------

    @Test
    public void testStationsWithoutDataAreNotShownWhenFiltering() {
        initPresenterWith(Arrays.asList(sNull, s3));

        sut.onFilterApplied("1");

        verify(mockView).showStations(Arrays.asList(s3));
    }

    @Test
    public void testOnlyStationsWithoutDataShowsNoResults() {
        initPresenterWith(Arrays.asList(sNull));

        sut.onFilterApplied("1");

        verify(mockView).showNoStationsWithMinFreeBikes(1);
    }
}
