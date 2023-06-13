package com.example.atry;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WolframAlphaService {
    @GET("query")
    Call<WolframResponse> getQueryResult(@Query("APP_ID") String apiKey, @Query("input") String input);
}
