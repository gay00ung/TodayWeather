package com.example.todayweather

import android.content.pm.PackageManager
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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.time.LocalDate
import java.time.LocalTime
import android.Manifest

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

private lateinit var fusedLocationClient: FusedLocationProviderClient
private val LOCATION_PERMISSION_REQUEST_CODE = 1000

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

    var base_date = LocalDate.now().toString()
    var base_time = LocalTime.now().toString()
    var nx = "55"
    var ny = "127"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            getLastLocation()
        } else {
            requestLocationPermission()
        }

        temp_tv = findViewById(R.id.temp_tv)
        tempMorning_tv = findViewById(R.id.tempMorning_tv)
        tempDayTime_tv = findViewById(R.id.tempDayTime_tv)
        humidity_tv = findViewById(R.id.humidity_tv)
        sky_tv = findViewById(R.id.sky_tv)
        rain_tv = findViewById(R.id.rain_tv)
        rainType_tv = findViewById(R.id.rainType_tv)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun getLastLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val gridCoordinate = convertToGrid(location.latitude, location.longitude)
                        nx = gridCoordinate.first.toString()
                        ny = gridCoordinate.second.toString()
                        setWeather(nx, ny)
                    }
                }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setWeather(nx: String, ny: String) {
        val cal = Calendar.getInstance()
        val time = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time)

        Log.d("base date : $base_date", "base time : $base_time")

        base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        base_time = getTime(time)

        if(base_time >= "2000") { // PM8 기준
            cal.add(Calendar.DATE, -1).toString()
            base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }

        val call = ApiObject.retrofitService.getWeather(1, 10000, "JSON", base_date, base_time, nx, ny)

        call.enqueue(object: retrofit2.Callback<WEATHER> {
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                if(response.isSuccessful) {
                    val it: List<ITEM> = response.body()!!.response.body.items.item
                    var temp = ""
                    var tempMin = ""
                    var tempDayTime = ""
                    var humidity = ""
                    var sky = ""
                    var rain = ""
                    var rainType = ""

                    for (item in it) {
                        when (item.category) {
                            "TMP" -> temp = item.fcstValue
                            "TMN" -> tempMin = item.fcstValue
                            "TMX" -> tempDayTime = item.fcstValue
                            "REH" -> humidity = item.fcstValue
                            "SKY" -> sky = item.fcstValue
                            "POP" -> rain = item.fcstValue
                            "PTY" -> rainType = item.fcstValue
                            else -> continue
                        }
                    }
                    setWeather(temp, tempMin, tempDayTime, humidity, sky, rain, rainType)
                    Log.d("API Response", response.body().toString())
                    Toast.makeText(applicationContext, it[0].fcstDate + ", " + it[0].fcstTime + "의 날씨 정보입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                Toast.makeText(applicationContext, "날씨 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun setWeather(temp: String, tempMorning: String, tempDayTime: String, humidity: String, sky: String, rain: String, rainType: String) {
        temp_tv.text = "$temp°C"

        tempMorning_tv.text = "$tempMorning°C"

        tempDayTime_tv.text = "$tempDayTime°C"

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

fun convertToGrid(lat: Double, lon: Double): Pair<Int, Int> {
    val RE = 6371.00877 // 지구 반경(km)
    val GRID = 5.0 // 격자 간격(km)
    val SLAT1 = 30.0 // 투영 위도1(degree)
    val SLAT2 = 60.0 // 투영 위도2(degree)
    val OLON = 126.0 // 기준점 경도(degree)
    val OLAT = 38.0 // 기준점 위도(degree)
    val XO = 43 // 기준점 X좌표(GRID)
    val YO = 136 // 기준점 Y좌표(GRID)

    val DEGRAD = Math.PI / 180.0
    val re = RE / GRID
    val slat1 = SLAT1 * DEGRAD
    val slat2 = SLAT2 * DEGRAD
    val olon = OLON * DEGRAD
    val olat = OLAT * DEGRAD

    var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
    sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
    var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
    sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
    var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
    ro = re * sf / Math.pow(ro, sn)

    var ra = Math.tan(Math.PI * 0.25 + (lat) * DEGRAD * 0.5)
    ra = re * sf / Math.pow(ra, sn)
    var theta = lon * DEGRAD - olon
    if (theta > Math.PI) theta -= 2.0 * Math.PI
    if (theta < -Math.PI) theta += 2.0 * Math.PI
    theta *= sn

    val x = Math.floor(ra * Math.sin(theta) + XO + 0.5).toInt()
    val y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5).toInt()

    return Pair(x, y)
}
