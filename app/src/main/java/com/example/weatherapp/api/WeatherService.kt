package com.example.weatherapp.api

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherService (private val context : Context) {
    private var weatherAPI: WeatherServiceAPI

    private val imageLoader = ImageLoader.Builder(context)
        .allowHardware(false).build()

    init {
        val retrofitAPI = Retrofit.Builder()
            .baseUrl(WeatherServiceAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        weatherAPI = retrofitAPI.create(WeatherServiceAPI::class.java)
    }

    suspend fun getName(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        search("$lat,$lng")?.name
    }

    suspend fun getLocation(name: String): LatLng? = withContext(Dispatchers.IO) {
        val loc = search(name)
        if (loc != null && loc.lat != null && loc.lon != null) {
            LatLng(loc.lat!!, loc.lon!!)
        } else null
    }

    private fun search(query: String) : APILocation? {
        val call = weatherAPI.search(query)
        val apiLoc = call.execute().body()
        return if (!apiLoc.isNullOrEmpty()) apiLoc[0] else null
    }

    suspend fun getWeather(name: String): APICurrentWeather? =
        withContext(Dispatchers.IO) {
            val call = weatherAPI.weather(name)
            call.execute().body()
        }

    suspend fun getForecast(name: String) : APIWeatherForecast? =
        withContext(Dispatchers.IO) {
            val call = weatherAPI.forecast(name)
            call.execute().body()
        }

    suspend fun getBitmap(imgUrl: String) : Bitmap? = withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context).data(imgUrl)
            .allowHardware(false).build()
        val response = imageLoader.execute(request)
        response.drawable?.toBitmap()
    }
}
