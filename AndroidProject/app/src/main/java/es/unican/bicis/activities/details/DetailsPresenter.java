package es.unican.bicis.activities.details;

import es.unican.bicis.model.Location;
import es.unican.bicis.model.Network;

public class DetailsPresenter implements IDetailsContract.Presenter{
    /** Text shown when a value is not available or is invalid */
    static final String NOT_AVAILABLE = "-";

    /** Text shown for a true boolean value */
    static final String YES = "Sí";

    /** Text shown for a false boolean value */
    static final String NO = "No";

    /** The view that is controlled by this presenter */
    private IDetailsContract.View view;

    /**
     * @see IDetailsContract.Presenter#init(IDetailsContract.View, Network)
     * @param view the view to control
     * @param network the network whose details must be shown
     */
    @Override
    public void init(IDetailsContract.View view, Network network) {
        this.view = view;

        if (network == null) {
            view.showNoDetailsAvailable();
            return;
        }

        // Header: always shown (also in the "no details" case, as in the mock-up)
        view.showName(textOrNotAvailable(network.getName()));
        view.showLocation(formatLocation(network.getLocation()));

        // Prueba 4: the network has no data beyond name and city
        if (network.hasOnlyBasicInfo()) {
            view.showNoDetailsAvailable();
            return;
        }

        // Prueba 1, 2 y 3: show each value, or "-" if missing or invalid
        Location location = network.getLocation();
        view.showLatitude(formatLatitude(location));
        view.showLongitude(formatLongitude(location));
        view.showEbikes(formatBoolean(network.getEbikes()));

        Integer operatorsCount = network.countOperators();
        if (operatorsCount == null) {
            view.showOperatorsCount(NOT_AVAILABLE);
            view.showOperators(NOT_AVAILABLE);
        } else {
            view.showOperatorsCount(String.valueOf(operatorsCount));
            view.showOperators(formatOperators(network.getCompany()));
        }
    }

    /**
     * Returns the given text, or NOT_AVAILABLE if it is null or blank.
     * @param text the text to check
     * @return the text, or "-"
     */
    private String textOrNotAvailable(String text) {
        if (text == null || text.trim().isEmpty()) {
            return NOT_AVAILABLE;
        }
        return text;
    }

    /**
     * Builds the location text with the city and the country of the network.
     * @param location the location of the network
     * @return "city, country", only one of them if the other is missing, or "-" if both are missing
     */
    private String formatLocation(Location location) {
        if (location == null) {
            return NOT_AVAILABLE;
        }
        String city = textOrNotAvailable(location.getCity());
        String country = textOrNotAvailable(location.getCountry());

        if (city.equals(NOT_AVAILABLE)) {
            return country;
        }
        if (country.equals(NOT_AVAILABLE)) {
            return city;
        }
        return city + ", " + country;
    }

    /**
     * Formats the latitude of the network.
     * @param location the location of the network
     * @return the latitude as text, or "-" if it is missing or out of range [-90, 90]
     */
    private String formatLatitude(Location location) {
        if (location == null || location.getLatitude() == null) {
            return NOT_AVAILABLE;
        }
        double latitude = location.getLatitude();
        if (latitude < -90 || latitude > 90) {
            return NOT_AVAILABLE;
        }
        return String.valueOf(latitude);
    }

    /**
     * Formats the longitude of the network.
     * @param location the location of the network
     * @return the longitude as text, or "-" if it is missing or out of range [-180, 180]
     */
    private String formatLongitude(Location location) {
        if (location == null || location.getLongitude() == null) {
            return NOT_AVAILABLE;
        }
        double longitude = location.getLongitude();
        if (longitude < -180 || longitude > 180) {
            return NOT_AVAILABLE;
        }
        return String.valueOf(longitude);
    }

    /**
     * Translates a boolean value into the text shown to the user.
     * A null value means the data is not available, which is different from false.
     * @param value the boolean value
     * @return "Sí", "No", or "-" if null
     */
    private String formatBoolean(Boolean value) {
        if (value == null) {
            return NOT_AVAILABLE;
        }
        return value ? YES : NO;
    }

    /**
     * Builds the text with the names of the operating companies, one per line.
     * @param companies the names of the companies (already validated)
     * @return the formatted list, e.g. "• A\n• B"
     */
    private String formatOperators(String[] companies) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < companies.length; i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append("• ").append(companies[i].trim());
        }
        return sb.toString();
    }
}
