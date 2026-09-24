package es.unican.bicis.repository;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Service singletons for the <a href="https://api.citybik.es/v2/">CityBikes API</a>
 */
public class CityBikesService {

    /** CityBikes API base URL */
    final static String BASE_URL = "https://api.citybik.es/";

    /** Retrofit object for the CityBikes API */
    public static final Retrofit retrofit;

    /** Object to access the CityBikes API */
    public static final ICityBikesAPI api;

    static {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .header("User-Agent", "Bicis-2026/1.0")
                        .build()))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ICityBikesAPI.class);
    }

}
