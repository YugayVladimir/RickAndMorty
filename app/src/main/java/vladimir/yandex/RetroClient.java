package vladimir.yandex;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vladimir.yandex.interfaces.ApiService;


public class RetroClient {

    private static final String ROOT_URL = "https://rickandmortyapi.com/api/";

    private static Retrofit getRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }
}