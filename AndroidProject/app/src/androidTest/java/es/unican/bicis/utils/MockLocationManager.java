package es.unican.bicis.utils;

import android.Manifest;
import android.app.UiAutomation;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Replaces the real GPS provider of the device with a test provider so that
 * instrumented tests can decide which position the application sees.
 *
 * <p>Mocking the location requires three steps, all of them done by {@link #setUp()}:</p>
 * <ol>
 *     <li>The {@code android:mock_location} app op must be allowed for the application under
 *     test. It can only be granted from a shell (adb), so it is executed here through the
 *     {@link UiAutomation} of the instrumentation.</li>
 *     <li>The location permissions must be granted, together with their per-UID app ops.
 *     Otherwise the system returns a null position even though the permission is granted.</li>
 *     <li>A test provider named {@code gps} must be registered. From that moment the test
 *     provider hides the real one, and its position is fully controlled with
 *     {@link #setLocation(double, double, float)}.</li>
 * </ol>
 *
 * <p>Typical usage in an instrumented test:</p>
 * <pre>
 *     MockLocationManager mockLocation = new MockLocationManager(targetContext);
 *     mockLocation.setUp();                       // in @Before
 *     mockLocation.setLocation(43.4623, -3.8099); // the position the app will see
 *     mockLocation.awaitMockedLocation(5000);     // wait until the app can read it
 *     ...                                         // assertions
 *     mockLocation.tearDown();                    // in @After
 * </pre>
 *
 * <p>Remember that the application must declare the location permissions in its manifest
 * (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION).</p>
 */
public class MockLocationManager {

    /** Provider name that is mocked. The application reads its position through it. */
    private static final String PROVIDER_NAME = LocationManager.GPS_PROVIDER;

    /** Time between two reads of the last known position, in milliseconds. */
    private static final long POLL_INTERVAL_MS = 100;

    /** Tolerance used to decide whether a position is the mocked one. */
    private static final double COORDINATE_DELTA = 1e-6;

    /** Context of the application under test (not the test application). */
    private final Context context;

    private final LocationManager locationManager;

    /** Last position injected, used to re-send it while a test waits for it. */
    private Location lastMockedLocation;

    /** {@code true} once the test provider is registered. */
    private boolean providerAdded;

    public MockLocationManager(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (this.locationManager == null) {
            throw new IllegalStateException("The device does not provide a LocationManager");
        }
    }

    /**
     * Prepares the device to accept mock locations and registers the GPS test provider.
     * It also grants the location permissions to the application under test.
     */
    @SuppressWarnings("deprecation") // the non deprecated overload only exists from API 31
    public void setUp() {
        String packageName = context.getPackageName();

        // The app op can only be changed by adb/shell, never from the app itself
        executeShellCommand("appops set " + packageName + " android:mock_location allow");
        grantLocationPermissions(packageName);

        // Be idempotent: a previous test may have left a test provider behind
        removeTestProviderQuietly();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ProviderProperties properties = new ProviderProperties.Builder()
                    .setHasNetworkRequirement(false)      // GPS does not need the network
                    .setHasSatelliteRequirement(true)
                    .setHasCellRequirement(false)
                    .setHasMonetaryCost(false)
                    .setHasAltitudeSupport(true)
                    .setHasSpeedSupport(true)
                    .setHasBearingSupport(true)
                    .setPowerUsage(ProviderProperties.POWER_USAGE_LOW)
                    .setAccuracy(ProviderProperties.ACCURACY_FINE)
                    .build();
            locationManager.addTestProvider(PROVIDER_NAME, properties);
        } else {
            locationManager.addTestProvider(
                    PROVIDER_NAME,
                    false, // requiresNetwork
                    true,  // requiresSatellite
                    false, // requiresCell
                    false, // hasMonetaryCost
                    true,  // supportsAltitude
                    true,  // supportsSpeed
                    true,  // supportsBearing
                    Criteria.POWER_LOW,
                    Criteria.ACCURACY_FINE);
        }
        providerAdded = true;

        locationManager.setTestProviderEnabled(PROVIDER_NAME, true);
    }

    /**
     * Injects a position as if it had been obtained from the GPS.
     *
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @param accuracy  horizontal accuracy in meters
     */
    public void setLocation(double latitude, double longitude, float accuracy) {
        Location location = new Location(PROVIDER_NAME);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);
        location.setAltitude(0.0);
        location.setBearing(0.0f);
        location.setSpeed(0.0f);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        // The three fields below are mandatory in recent Android versions
        location.setVerticalAccuracyMeters(0.1f);
        location.setSpeedAccuracyMetersPerSecond(0.01f);
        location.setBearingAccuracyDegrees(0.1f);

        locationManager.setTestProviderLocation(PROVIDER_NAME, location);
        lastMockedLocation = location;
    }

    /**
     * Same as {@link #setLocation(double, double, float)} using 1 meter of accuracy.
     */
    public void setLocation(double latitude, double longitude) {
        setLocation(latitude, longitude, 1.0f);
    }

    /**
     * Waits until the mocked position is available as the last known position.
     *
     * <p>The last known position is what an activity reads when it wants the current position
     * without waiting for a new GPS fix. The position is injected again on every iteration
     * because some devices take a moment to process it. A known position is only accepted when
     * it has the injected coordinates, so a real GPS fix taken before the test cannot be
     * mistaken for the mock.</p>
     *
     * @param timeoutMs maximum time to wait, in milliseconds
     * @return the mocked position, or {@code null} if it was not available in time
     */
    public Location awaitMockedLocation(long timeoutMs) throws InterruptedException {
        Assert.assertNotNull("No position has been injected yet", lastMockedLocation);

        long deadline = SystemClock.elapsedRealtime() + timeoutMs;
        Location lastKnown = locationManager.getLastKnownLocation(PROVIDER_NAME);
        while (!isMockedPosition(lastKnown) && SystemClock.elapsedRealtime() < deadline) {
            locationManager.setTestProviderLocation(PROVIDER_NAME, lastMockedLocation);
            Thread.sleep(POLL_INTERVAL_MS);
            lastKnown = locationManager.getLastKnownLocation(PROVIDER_NAME);
        }
        return isMockedPosition(lastKnown) ? lastKnown : null;
    }

    /**
     * Restores the real GPS provider and withdraws the mock location permission.
     */
    public void tearDown() {
        if (providerAdded) {
            removeTestProviderQuietly();
            providerAdded = false;
        }
        executeShellCommand("appops set " + context.getPackageName() + " android:mock_location default");
    }

    private boolean isMockedPosition(Location location) {
        return location != null
                && Math.abs(location.getLatitude() - lastMockedLocation.getLatitude()) < COORDINATE_DELTA
                && Math.abs(location.getLongitude() - lastMockedLocation.getLongitude()) < COORDINATE_DELTA;
    }

    private void removeTestProviderQuietly() {
        try {
            locationManager.removeTestProvider(PROVIDER_NAME);
        } catch (IllegalArgumentException e) {
            // The provider was not added or was already removed
        }
    }

    /**
     * Grants the location permissions to the application under test.
     *
     * <p>The app ops are set explicitly with {@code --uid} because location app ops are per UID
     * and, in some devices or test environments, they stay ignored after installing the
     * application. In that state the system returns a null position even though the runtime
     * permission is granted.</p>
     */
    private void grantLocationPermissions(String packageName) {
        // pm grant is idempotent, so it is executed even when the permission is already granted
        executeShellCommand("pm grant " + packageName + " " + Manifest.permission.ACCESS_FINE_LOCATION);
        executeShellCommand("pm grant " + packageName + " " + Manifest.permission.ACCESS_COARSE_LOCATION);
        executeShellCommand("appops set --uid " + packageName + " android:fine_location allow");
        executeShellCommand("appops set --uid " + packageName + " android:coarse_location allow");
    }

    /**
     * Executes a shell command through the instrumentation (the same as
     * {@code UiDevice.executeShellCommand}) and waits for it to finish.
     */
    private void executeShellCommand(String command) {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        ParcelFileDescriptor descriptor = uiAutomation.executeShellCommand(command);
        try (FileInputStream input = new ParcelFileDescriptor.AutoCloseInputStream(descriptor)) {
            byte[] buffer = new byte[1024];
            while (input.read(buffer) != -1) {
                // Drain the output so the command is completed before returning
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot execute shell command: " + command, e);
        }
    }
}