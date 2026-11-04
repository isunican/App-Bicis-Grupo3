package es.unican.bicis.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import es.unican.bicis.model.Station;
import es.unican.bicis.model.StationExtra;

public class StationUtilsTest {

    private Station estacion(String nombre, Integer bicis) {
        Station s = new Station();
        s.setName(nombre);
        s.setFreeBikes(bicis);
        return s;
    }

    @Test
    public void ordenaDeMasAMenosBicis() {
        List<Station> lista = Arrays.asList(
                estacion("A", 3), estacion("B", 10), estacion("C", 0));

        List<Station> ordenada = StationUtils.sortByFreeBikes(lista);

        assertEquals("B", ordenada.get(0).getName());
        assertEquals("A", ordenada.get(1).getName());
        assertEquals("C", ordenada.get(2).getName());
    }

    @Test
    public void estacionesSinDatoDeBicisVanAlFinal() {
        List<Station> lista = Arrays.asList(
                estacion("A", null), estacion("B", 2), estacion("C", 5));

        List<Station> ordenada = StationUtils.sortByFreeBikes(lista);

        assertEquals("C", ordenada.get(0).getName());
        assertEquals("B", ordenada.get(1).getName());
        assertEquals("A", ordenada.get(2).getName());
    }

    @Test
    public void listaNullDevuelveListaVacia() {
        assertTrue(StationUtils.sortByFreeBikes(null).isEmpty());
    }

    @Test
    public void datoAusenteSeMuestraConGuion() {
        Station s = new Station();   // sin extra, como en TUeBICI

        assertEquals("-", StationUtils.formatValue(s.getEbikes()));
        assertEquals("-", StationUtils.formatValue(s.getFreeBikes()));
        assertEquals("-", StationUtils.formatText(s.getName()));
    }

    @Test
    public void datoPresenteSeMuestraTalCual() {
        assertEquals("0", StationUtils.formatValue(0));
        assertEquals("7", StationUtils.formatValue(7));
        assertEquals("Plaza Mayor", StationUtils.formatText("Plaza Mayor"));
    }

    // ---------- formatBikes (ULE.2 del plan de pruebas) ----------

    @Test
    public void formatBikesTodosLosDatos() {
        assertEquals("5/12", StationUtils.formatBikes(5, 7, null));    // ULE.2a
        assertEquals("0/20", StationUtils.formatBikes(0, 20, null));   // ULE.2b
        assertEquals("12/12", StationUtils.formatBikes(12, 0, null));  // ULE.2c
        assertEquals("0/0", StationUtils.formatBikes(0, 0, null));     // ULE.2d
    }

    @Test
    public void formatBikesDatosAusentes() {
        assertEquals("-/12", StationUtils.formatBikes(null, 4, 12));   // ULE.2e
        assertEquals("-/-", StationUtils.formatBikes(null, 4, null));  // ULE.2f
        assertEquals("5/-", StationUtils.formatBikes(5, null, null));  // ULE.2g
        assertEquals("-/-", StationUtils.formatBikes(null, null, null)); // ULE.2h
    }

    @Test
    public void formatBikesSumaMandaSobreSlots() {
        assertEquals("5/12", StationUtils.formatBikes(5, 7, 20));      // ULE.2k
    }

    // ---------- hasEbikes (ULE.3 del plan de pruebas) ----------

    private Station conEbikes(Integer ebikes) {
        StationExtra extra = new StationExtra();
        extra.setEbikes(ebikes);
        Station s = new Station();
        s.setExtra(extra);
        return s;
    }

    @Test
    public void hasEbikesConBicisElectricas() {
        assertTrue(StationUtils.hasEbikes(conEbikes(2)));    // ULE.3a
        assertTrue(StationUtils.hasEbikes(conEbikes(1)));    // ULE.3b
    }

    @Test
    public void hasEbikesSinBicisElectricasONoSeSabe() {
        assertFalse(StationUtils.hasEbikes(conEbikes(0)));      // ULE.3c
        assertFalse(StationUtils.hasEbikes(conEbikes(null)));   // ULE.3d
        assertFalse(StationUtils.hasEbikes(new Station()));     // ULE.3e
        assertFalse(StationUtils.hasEbikes(null));
    }

    // Ordenación por huecos libres (HU 532335)

    private Station estacion(String nombre, Integer bicis, Integer huecos) {
        Station s = estacion(nombre, bicis);
        s.setEmptySlots(huecos);
        return s;
    }

    @Test
    public void ordenaDeMasAMenosHuecos() {
        List<Station> lista = Arrays.asList(
                estacion("A", 5, 4), estacion("B", 0, 20), estacion("C", 2, 9));

        List<Station> ordenada = StationUtils.sortByEmptySlots(lista);

        assertEquals("B", ordenada.get(0).getName());
        assertEquals("C", ordenada.get(1).getName());
        assertEquals("A", ordenada.get(2).getName());
        assertEquals("A", lista.get(0).getName());   // la lista original no cambia
    }

    @Test
    public void aIgualdadDeHuecosVaPrimeroLaQueTieneMasBicis() {
        List<Station> lista = Arrays.asList(
                estacion("Correos", 0, 19), estacion("Castilla Hermida", 1, 19));

        List<Station> ordenada = StationUtils.sortByEmptySlots(lista);

        assertEquals("Castilla Hermida", ordenada.get(0).getName());
        assertEquals("Correos", ordenada.get(1).getName());
    }

    @Test
    public void estacionesSinDatoDeHuecosVanAlFinal() {
        List<Station> lista = Arrays.asList(
                estacion("A", 1, null), estacion("B", 1, 3));

        List<Station> ordenada = StationUtils.sortByEmptySlots(lista);

        assertEquals("B", ordenada.get(0).getName());
        assertEquals("A", ordenada.get(1).getName());
    }

    @Test
    public void ordenarPorHuecosUnaListaNullDevuelveListaVacia() {
        assertTrue(StationUtils.sortByEmptySlots(null).isEmpty());
    }
}
