package com.example.weatherapp

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.api.WeatherService
import com.example.weatherapp.db.fb.FBDatabase
import com.example.weatherapp.db.local.LocalDatabase
import com.example.weatherapp.monitor.ForecastMonitor
import com.example.weatherapp.repo.Repository
import com.example.weatherapp.ui.CityDialog
import com.example.weatherapp.ui.nav.BottomNavBar
import com.example.weatherapp.ui.nav.BottomNavItem
import com.example.weatherapp.ui.nav.MainNavHost
import com.example.weatherapp.ui.nav.Route
import com.example.weatherapp.ui.theme.WeatherappTheme
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherappTheme {
                val navController = rememberNavController()
                val fbDB = remember { FBDatabase() }
                val localDB = remember {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
                    LocalDatabase(this, uid)
                }
                val repository = remember { Repository(fbDB, localDB) }
                val weatherService = remember { WeatherService(this) }
                val forecastMonitor = remember { ForecastMonitor(this) }
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(repository, weatherService, forecastMonitor)
                )
                var showDialog by remember { mutableStateOf(false) }

                val user = viewModel.user.collectAsStateWithLifecycle(null).value

                val currentRoute = navController.currentBackStackEntryAsState()
                val showButton = currentRoute.value?.destination?.hasRoute(Route.List::class) == true
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(), onResult = {})

                if (showDialog) CityDialog(
                    onDismiss = { showDialog = false },
                    onConfirm = { city ->
                        if (city.isNotBlank()) viewModel.addCity(city)
                        showDialog = false
                    }
                )

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                val name = user?.name ?: "[carregando...]"
                                Text("Bem-vindo/a! $name")
                            },
                            actions = {
                                IconButton(onClick = { FirebaseAuth.getInstance().signOut() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Localized description"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        val items = listOf(
                            BottomNavItem.HomeButton,
                            BottomNavItem.ListButton,
                            BottomNavItem.MapButton,
                        )
                        BottomNavBar(viewModel, navController, items)
                    },
                    floatingActionButton = {
                        if (showButton) {
                            FloatingActionButton(onClick = { showDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar")
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        MainNavHost(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                    LaunchedEffect(viewModel.page) {
                        navController.navigate(viewModel.page) {
                            navController.graph.startDestinationRoute?.let {
                                popUpTo(it) {
                                    saveState = true
                                }
                            }
                            restoreState = true
                            launchSingleTop = true
                        }
                    }
                    DisposableEffect(Unit) {
                        val listener = Consumer<Intent> { intent ->
                            viewModel.city = intent.getStringExtra("city")
                            viewModel.page = Route.Home
                        }
                        addOnNewIntentListener(listener)
                        onDispose { removeOnNewIntentListener(listener) }
                    }
                }
            }
        }
    }
}
