
package es.unican.bicis.activities.main;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;

import android.Manifest;
import android.content.Context;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import es.unican.bicis.R;
import es.unican.bicis.utils.MockLocationManager;

/**
 * UI test for successful city detection using a mocked GPS location.
 * HU 532319 - Detect the user's city automatically.
 */
@HiltAndroidTest
public class DetectarCiudadExitoUITest {

    private static final double SANTANDER_LATITUDE = 43.4623057;
    private static final double SANTANDER_LONGITUDE = -3.8099803;
    private static final long LOCATION_TIMEOUT_MS = 15000;

    @Rule(order = 0)
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public GrantPermissionRule permissions = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    private MockLocationManager mockLocationManager;
    private ActivityScenario<MainView> scenario;
    private View decorView;

    /**
     * Sets the mocked GPS location to Santander before launching the activity.
     *
     * @throws InterruptedException if the waiting thread is interrupted
     */
    @Before
    public void setUp() throws InterruptedException {
        Context context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();

        mockLocationManager = new MockLocationManager(context);
        mockLocationManager.setUp();
        mockLocationManager.setLocation(
                SANTANDER_LATITUDE,
                SANTANDER_LONGITUDE
        );

        assertNotNull(
                "The application did not receive the mocked GPS location",
                mockLocationManager.awaitMockedLocation(LOCATION_TIMEOUT_MS)
        );
    }

    /**
     * Closes the activity and restores the GPS configuration.
     */
    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }

        if (mockLocationManager != null) {
            mockLocationManager.tearDown();
        }
    }

    /**
     * Verifies that the application detects Santander from the mocked
     * GPS location and displays the corresponding confirmation message.
     *
     * @throws InterruptedException if the waiting thread is interrupted
     */
    @Test
    public void testDetectarCiudadCorrectamente() throws InterruptedException {
        scenario = ActivityScenario.launch(MainView.class);

        scenario.onActivity(activity ->
                decorView = activity.getWindow().getDecorView()
        );

        String expectedMessage = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getString(R.string.cityDetected, "Santander");

        for (int i = 0; i < 20; i++) {
            try {
                onView(withText(expectedMessage))
                        .inRoot(withDecorView(not(decorView)))
                        .check(matches(isDisplayed()));
                return;
            } catch (RuntimeException | AssertionError e) {
                if (i == 19) {
                    throw e;
                }
                Thread.sleep(500);
            }
        }
    }
}
