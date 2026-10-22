package es.unican.bicis.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import es.unican.bicis.model.Station;

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
}
