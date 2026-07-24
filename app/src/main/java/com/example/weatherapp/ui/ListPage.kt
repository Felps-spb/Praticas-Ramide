package com.example.weatherapp.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.R
import com.example.weatherapp.model.City
import com.example.weatherapp.model.Weather

@Composable
fun ListPage(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val cityMap = viewModel.cities.collectAsStateWithLifecycle(initialValue = emptyMap<String, City>()).value
    val cityList = cityMap.values.toList().sortedBy { it.name }
    val weatherMap = viewModel.weather.collectAsStateWithLifecycle(initialValue = emptyMap<String, Weather>()).value
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(items = cityList, key = { it.name }) { city ->
            LaunchedEffect(city.name) {
                viewModel.loadWeather(city.name)
            }
            val weather = weatherMap[city.name] ?: Weather.LOADING
            CityItem(
                city = city,
                weather = weather,
                onClick = {
                    viewModel.city = city.name
                    viewModel.page = com.example.weatherapp.ui.nav.Route.Home
                },
                onClose = {
                    viewModel.remove(city)
                    Toast.makeText(context, "${city.name} removida", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun CityItem(
    city: City,
    weather: Weather,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val desc = if (weather == Weather.LOADING) "Carregando clima..." else weather.desc
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = weather.imgUrl,
            modifier = modifier.size(75.dp),
            error = painterResource(id = R.drawable.loading),
            contentDescription = "Imagem"
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = modifier.weight(1f)) {
            Text(
                modifier = Modifier,
                text = city.name,
                fontSize = 24.sp
            )
            Text(
                modifier = Modifier,
                text = desc,
                fontSize = 16.sp
            )
        }
        val icon = if (city.isMonitored) Icons.Filled.Notifications else Icons.Outlined.Notifications
        Icon(imageVector = icon, contentDescription = "Monitorada",
            modifier = Modifier.size(28.dp))
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Close")
        }
    }
}
