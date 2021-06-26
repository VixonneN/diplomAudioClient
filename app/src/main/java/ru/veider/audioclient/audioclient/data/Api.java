package ru.veider.audioclient.audioclient.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Api {

  String HOST = "https://www.googleapis.com/books/v1/";
  String API_KEY = "AIzaSyCiIo7TmN2vJ4yq7YHmU4znwPuvU7nj7lU";

  @GET("volumes?key=" + API_KEY)
  Call<SearchResponse> searchFilm(@Query("q") String query);

  @GET("volumes/{id}?key=" + API_KEY)
  Call<Film> getFilm(@Path("id") String id);
}
