package es.unican.bicis.model;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Gets the countries and the cities that have bike sharing networks, and filters the networks
 * by country and city, so that the user can select the city where they are going to use the bikes
 * (HU 532301 Seleccionar la ciudad).
 *
 * Countries are identified by their ISO 3166 code, as given by the
 * <a href="https://api.citybik.es/v2/">CityBikes API</a> (for example, "ES"), and are shown
 * with their name in Spanish (for example, "España").
 *
 * Only networks that report both their country and their city are offered in the selectors.
 * Texts are compared without taking into account the surrounding spaces nor the case, so
 * "Madrid" and "madrid " are the same city. Names are sorted with the Spanish rules, so a name
 * with an accent (for example, "Ávila") is sorted in its place and not at the end.
 */
public class CitySelector {

    /** Locale used to get the names of the countries and to sort the names */
    private static final Locale SPANISH = new Locale("es", "ES");

    /**
     * Private constructor, this class only has static methods.
     */
    private CitySelector() {
    }

    /**
     * Returns the codes of the countries that have at least one network with country and city,
     * without duplicates and sorted alphabetically by their name in Spanish
     * (for example, "DE" (Alemania) goes before "AR" (Argentina)).
     *
     * Precondition: networks is not null (it can contain null elements, which are skipped).
     *
     * @param networks the networks
     * @return a new list with the country codes in upper case; empty if no network
     *         reports its country and its city
     */
    public static List<String> getCountries(List<Network> networks) {
        List<String> countries = new ArrayList<>();
        for (Network network : networks) {
            if (hasCountryAndCity(network)) {
                String code = normalize(network.getLocation().getCountry()).toUpperCase(Locale.ROOT);
                if (!countries.contains(code)) {
                    countries.add(code);
                }
            }
        }
        Collator collator = spanishCollator();
        countries.sort(Comparator.comparing(CitySelector::getCountryName, collator));
        return countries;
    }

    /**
     * Returns the name in Spanish of a country from its ISO 3166 code
     * (for example, "ES" returns "España").
     *
     * @param countryCode the code of the country (case and surrounding spaces are ignored)
     * @return the name of the country in Spanish; the code itself if it is not a known code;
     *         null if the code is null
     */
    public static String getCountryName(String countryCode) {
        if (countryCode == null) {
            return null;
        }
        String code = countryCode.trim().toUpperCase(Locale.ROOT);
        String name = new Locale("", code).getDisplayCountry(SPANISH);
        if (name.isEmpty()) {
            return code;
        } else {
            return name;
        }
    }

    /**
     * Returns the cities of a country that have at least one network, without duplicates
     * and sorted alphabetically with the Spanish rules. If a city has several networks it
     * appears only once, with the name (without surrounding spaces) of its first network.
     * Networks with the city null or blank are not taken into account.
     *
     * Precondition: networks is not null (it can contain null elements, which are skipped).
     *
     * @param networks the networks
     * @param countryCode the code of the country, or null if the option "Todos" is selected
     * @return a new list with the names of the cities; empty if the country is null, or
     *         it has no networks
     */
    public static List<String> getCities(List<Network> networks, String countryCode) {
        List<String> cities = new ArrayList<>();
        if (countryCode == null) {
            return cities;
        }
        for (Network network : networks) {
            if (hasCountryAndCity(network) && isInCountry(network, countryCode)) {
                String city = normalize(network.getLocation().getCity());
                if (!containsIgnoreCase(cities, city)) {
                    cities.add(city);
                }
            }
        }
        cities.sort(spanishCollator());
        return cities;
    }

    /**
     * Returns the networks of a country and a city, in the same order as in the given list.
     * If the city is null, all the networks of the country are returned (also those that do not
     * report their city). If the country is null (option "Todos"), all the networks are returned.
     *
     * Precondition: networks is not null.
     *
     * @param networks the networks
     * @param countryCode the code of the country, or null if the option "Todos" is selected
     * @param city the name of the city, or null if no city is selected
     * @return a new list with the networks that fulfil the condition; the given list
     *         is not modified
     */
    public static List<Network> filterByCity(List<Network> networks, String countryCode, String city) {
        List<Network> result = new ArrayList<>();
        if (countryCode == null) {
            result.addAll(networks);
            return result;
        }
        for (Network network : networks) {
            if (isInCountry(network, countryCode) && (city == null || isInCity(network, city))) {
                result.add(network);
            }
        }
        return result;
    }

    /**
     * Checks whether a network reports both its country and its city.
     *
     * @param network the network to check
     * @return true if the network and its location are not null and its country and city
     *         are not null nor blank
     */
    private static boolean hasCountryAndCity(Network network) {
        return network != null && network.getLocation() != null
                && !isBlank(network.getLocation().getCountry())
                && !isBlank(network.getLocation().getCity());
    }

    /**
     * Checks whether a network is in a country.
     *
     * @param network the network to check (can be null)
     * @param countryCode the code of the country
     * @return true if the country of the network is the given one, ignoring case and spaces
     */
    private static boolean isInCountry(Network network, String countryCode) {
        return network != null && network.getLocation() != null
                && network.getLocation().getCountry() != null
                && normalize(network.getLocation().getCountry()).equalsIgnoreCase(normalize(countryCode));
    }

    /**
     * Checks whether a network is in a city.
     *
     * @param network the network to check, already known to be not null and with location
     * @param city the name of the city
     * @return true if the city of the network is the given one, ignoring case and spaces
     */
    private static boolean isInCity(Network network, String city) {
        String networkCity = network.getLocation().getCity();
        return networkCity != null && normalize(networkCity).equalsIgnoreCase(normalize(city));
    }

    /**
     * Checks whether a list contains a text, ignoring case.
     *
     * @param list the list of texts
     * @param text the text to look for
     * @return true if some element of the list is equal to the text ignoring case
     */
    private static boolean containsIgnoreCase(List<String> list, String text) {
        for (String s : list) {
            if (s.equalsIgnoreCase(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the spaces at the beginning and at the end of a text.
     *
     * @param s the text, not null
     * @return the text without surrounding spaces
     */
    private static String normalize(String s) {
        return s.trim();
    }

    /**
     * Checks if a text is missing or only contains blank characters.
     *
     * @param s the text to check
     * @return true if s is null, empty or blank
     */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Creates a collator that sorts texts with the Spanish rules.
     *
     * @return the collator
     */
    private static Collator spanishCollator() {
        return Collator.getInstance(SPANISH);
    }
}
