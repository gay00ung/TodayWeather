package com.example.todayweather.network

import com.example.todayweather.model.WEATHER
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherInterface {
    @GET("getVilageFcst?serviceKey=${MySecretApi.API_KEY}")
    fun getWeather(
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("base_date") base_date: String,
        @Query("base_time") base_time: String,
        @Query("fcstDate") fcstDate: String,
        @Query("fcstTime") fcstTime: String,
        @Query("nx") nx: String,
        @Query("ny") ny: String
    ): Call<WEATHER>
}