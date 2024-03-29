package com.example.todayweather

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class WEATHER (val response: RESPONSE)
data class RESPONSE (val header: HEADER, val body: BODY)
data class HEADER (val resultCode: Int, val resultMessage: String)
data class BODY (val dataType: String, val items: ITEMS)
data class ITEMS (val item: List<ITEM>)
data class ITEM (val category: String, val fcstDate: String, val fcstTime: String, val fcstValue: String)

private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

object ApiObject {
    val retrofitService: WeatherInterface by lazy {
        retrofit.create(WeatherInterface::class.java)
    }
}

class MainActivity : AppCompatActivity() {
    lateinit var temp_tv : TextView
    lateinit var tempMorning_tv : TextView
    lateinit var tempDayTime_tv : TextView
    lateinit var humidity_tv : TextView
    lateinit var sky_tv : TextView
    lateinit var rain_tv : TextView
    lateinit var rainType_tv : TextView

    var base_date = "20240329"
    var base_time = "1400"
    var nx = "0"
    var ny = "0"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        temp_tv = findViewById(R.id.temp_tv)
        tempMorning_tv = findViewById(R.id.tempMorning_tv)
        tempDayTime_tv = findViewById(R.id.tempDayTime_tv)
        humidity_tv = findViewById(R.id.humidity_tv)
        sky_tv = findViewById(R.id.sky_tv)
        rain_tv = findViewById(R.id.rain_tv)
        rainType_tv = findViewById(R.id.rainType_tv)

        setWeather(nx, ny)
    }

    fun setWeather(nx: String, ny: String) {
        val cal = Calendar.getInstance()
        val time = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time)

        base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        base_time = getTime(time)

        if(base_time >= "2000") { // PM8 기준
            cal.add(Calendar.DATE, -1).toString()
            base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }

        val call = ApiObject.retrofitService.getWeather("JSON", 10, 1, base_date, base_time, nx, ny)

        call.enqueue(object: retrofit2.Callback<WEATHER> {
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                if(response.isSuccessful) {

                    // TODO: response.body.items.item is null!!! NPE crash! FIX IT!
                    val it: List<ITEM> = response.body()!!.response.body.items.item

                    var temp = ""
                    var tempMorning = ""
                    var tempDayTime = ""
                    var humidity = ""
                    var sky = ""
                    var rain = ""
                    var rainType = ""

                    for (i in 0..9) {
                        when (it[i].category) {
                            "T3H" -> temp = it[i].fcstValue
                            "TMN" -> tempMorning = it[i].fcstValue
                            "TMX" -> tempDayTime = it[i].fcstValue
                            "REH" -> humidity = it[i].fcstValue
                            "SKY" -> sky = it[i].fcstValue
                            "POP" -> rain = it[i].fcstValue
                            "PTY" -> rainType = it[i].fcstValue
                            else -> continue
                        }
                    }
                    setWeather(temp, tempMorning, tempDayTime, humidity, sky, rain, rainType)
                    Toast.makeText(applicationContext, it[0].fcstDate + ", " + it[0].fcstTime + "의 날씨 정보입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                Toast.makeText(applicationContext, "날씨 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun setWeather(temp: String, tempMorning: String, tempDayTime: String, humidity: String, sky: String, rain: String, rainType: String) {
        temp_tv.text = "$temp°"

        tempMorning_tv.text = "$tempMorning°"

        tempDayTime_tv.text = "$tempDayTime°"

        humidity_tv.text = "$humidity%"


        var skyResult = ""
        when(sky) {
            "1" -> skyResult = "맑음"
            "3" -> skyResult = "구름 많음"
            "4" -> skyResult = "흐림"
            else -> "Error"
        }
        sky_tv.text = skyResult

        rain_tv.text = "$rain%"

        var rainResult = ""
        when(rainType) {
            "0" -> rainResult = "없음"
            "1" -> rainResult = "비"
            "2" -> rainResult = "비/눈"
            "3" -> rainResult = "눈"
            "4" -> rainResult = "소나기"
            "5" -> rainResult = "빗방울"
            "6" -> rainResult = "빗방울/눈날림"
            "7" -> rainResult = "눈날림"
            else -> "Error"
        }
        rainType_tv.text = rainResult

    }

    fun getTime(time : String) : String {
        var result = ""
        when(time) {
            in "00".."02" -> result = "2000"    // 00~02
            in "03".."05" -> result = "2300"    // 03~05
            in "06".."08" -> result = "0200"    // 06~08
            in "09".."11" -> result = "0500"    // 09~11
            in "12".."14" -> result = "0800"    // 12~14
            in "15".."17" -> result = "1100"    // 15~17
            in "18".."20" -> result = "1400"    // 18~20
            else -> result = "1700"             // 21~23
        }
        return result
    }
}
