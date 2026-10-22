package es.unican.bicis.utils;

import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import es.unican.bicis.model.Network;
import es.unican.bicis.model.Station;

/**
 * Custom matchers for Espresso tests.
 */
public class Matchers {

    private Matchers() {
    }

    /** Red con el nombre dado (para onData sobre la lista de redes). */
    public static Matcher<Network> networkNamed(final String name) {
        return new TypeSafeMatcher<Network>() {
            @Override
            protected boolean matchesSafely(Network network) {
                return name.equals(network.getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("red llamada " + name);
            }
        };
    }

    /** Estación con el nombre dado (para onData sobre la lista de estaciones). */
    public static Matcher<Station> stationNamed(final String name) {
        return new TypeSafeMatcher<Station>() {
            @Override
            protected boolean matchesSafely(Station station) {
                return name.equals(station.getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("estación llamada " + name);
            }
        };
    }

    /** TextView su texto es un entero mayor o igual que min. */
    public static Matcher<View> hasNumberAtLeast(final int min) {
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            protected boolean matchesSafely(TextView textView) {
                try {
                    return Integer.parseInt(textView.getText().toString().trim()) >= min;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("TextView con un número >= " + min);
            }
        };
    }
}