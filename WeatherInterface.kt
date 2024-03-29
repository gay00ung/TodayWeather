package com.example.todayweather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherInterface {
    @GET("getVilageFcst?serviceKey=VsYylQTTXCGa9vPtbcmjnnbBfQKzuJN6v6SNroFjq5HPDC4XW4fRprVjXzLOl8faU3BYrthNdjXksgaWjT%2FVAA%3D%3D")
    fun getWeather(@Query("dataType") dataType: String,
                   @Query("numOfRows") numOfRows: Int,
                   @Query("pageNo") pageNo: Int,
                   @Query("base_date") base_date: String,
                   @Query("base_time") base_time: String,
                   @Query("nx") nx: String,
                   @Query("ny") ny: String)
    : Call<WEATHER>

}