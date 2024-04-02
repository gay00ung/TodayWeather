package com.example.todayweather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherInterface {
    @GET("getVilageFcst?serviceKey=IlCNInNG0GLfLkJUdxEaRfDfSV84q5PgtcdTPl61EKOlOdJckWovlSfd1%2FtXnFsDzlAwGewB%2FWqHPagUMh%2BJ4A%3D%3D")
    fun getWeather(@Query("pageNo") pageNo: Int,
                   @Query("numOfRows") numOfRows: Int,
                   @Query("dataType") dataType: String,
                   @Query("base_date") base_date: String,
                   @Query("base_time") base_time: String,
                   @Query("nx") nx: String,
                   @Query("ny") ny: String)
    : Call<WEATHER>

}