package es.unican.bicis.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.text.Collator;
import java.util.Locale;

import es.unican.bicis.model.Station;

/**
 * Métodos de ayuda para mostrar la lista de estaciones de una red:
 * ordenarlas por bicis disponibles y mostrar un guion en los datos ausentes.
 */
public class StationUtils {

    /** Texto que se muestra cuando un dato no está disponible. */
    public static final String NO_DATA = "-";

    private StationUtils() {
        // Clase de utilidades, no se instancia
    }

    /**
     * Devuelve una copia de la lista ordenada de más a menos bicis disponibles.
     * Las estaciones sin dato de bicis (null) van al final.
     * Si dos estaciones tienen las mismas bicis (o ninguna tiene el dato), se ordenan
     * por nombre sin distinguir mayúsculas ni tildes, y las que no tienen nombre van al final.
     * @param stations estaciones de la red (puede ser null)
     * @return nueva lista ordenada, o una lista vacía si stations es null
     */
    public static List<Station> sortByFreeBikes(List<Station> stations) {
        List<Station> sorted = new ArrayList<>();
        if (stations == null) {
            return sorted;
        }
        sorted.addAll(stations);
        Comparator<Integer> masAMenos = Comparator.nullsLast(Comparator.<Integer>reverseOrder());
        sorted.sort(Comparator.comparing(Station::getFreeBikes, masAMenos)
                .thenComparing(StationUtils::nameOrNull, byName()));
        return sorted;
    }

    /**
     * Comparador de nombres en español que no distingue mayúsculas ni tildes
     * ("Árboles" va antes que "Ayuntamiento"). Los null van al final.
     * @return el comparador
     */
    private static Comparator<String> byName() {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("es-ES"));
        collator.setStrength(Collator.PRIMARY);
        return Comparator.nullsLast(collator::compare);
    }

    /**
     * @param station la estación
     * @return su nombre, o null si no tiene o está en blanco
     */
    private static String nameOrNull(Station station) {
        String name = station.getName();
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return name.trim();
    }

    /**
     * Convierte un número en el texto que se muestra en la lista.
     * @param value el dato (bicis, huecos, bicis eléctricas...), puede ser null
     * @return el número como texto, o "-" si el dato no está disponible
     */
    public static String formatValue(Integer value) {
        if (value == null) {
            return NO_DATA;
        }
        return String.valueOf(value);
    }

    /**
     * Devuelve el texto que se muestra en la lista.
     * @param text el dato (por ejemplo, el nombre de la estación), puede ser null
     * @return el texto, o "-" si es null o está vacío
     */
    public static String formatText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return NO_DATA;
        }
        return text;
    }
}
