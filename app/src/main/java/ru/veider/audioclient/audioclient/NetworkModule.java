package ru.veider.audioclient.audioclient;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.veider.audioclient.audioclient.data.Api;

public class NetworkModule {
  private final Api api;

  public NetworkModule() {
    final OkHttpClient.Builder builder = new OkHttpClient.Builder();
    final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    builder.addInterceptor(loggingInterceptor);
    final Retrofit retrofit = new Retrofit.Builder() //
        .client(builder.build())//
        .baseUrl(Api.HOST) //
        .addConverterFactory(GsonConverterFactory.create()) //
        .build();

    api = retrofit.create(Api.class);
  }

  public Api api() {
    return api;
  }
}
