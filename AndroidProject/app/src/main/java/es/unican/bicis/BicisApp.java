package es.unican.bicis;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

/**
 * The Application class must be explicitly annotated for the dependency injection to work
 */
@HiltAndroidApp
public class BicisApp extends Application {

}