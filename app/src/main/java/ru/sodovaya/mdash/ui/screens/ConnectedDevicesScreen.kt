package ru.sodovaya.mdash.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.FadeTransition
import ru.sodovaya.mdash.R
import ru.sodovaya.mdash.composables.LocalScooterStatus
import ru.sodovaya.mdash.service.BluetoothForegroundService
import ru.sodovaya.mdash.service.ScooterData
import ru.sodovaya.mdash.ui.components.TabNavigationItem
import ru.sodovaya.mdash.ui.tabs.MainDashboardTab
import ru.sodovaya.mdash.ui.tabs.SettingsTab
import ru.sodovaya.mdash.utils.serializable


data class MainScreen(val device: String, val name: String): Screen {
    @SuppressLint("MissingPermission")
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ConnectedDeviceScreen(name, device) {
            navigator.replaceAll(ConnectionScreen())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("InlinedApi")
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ConnectedDeviceScreen(name: String, device: String, onClose: () -> Unit) {
    val context = LocalContext.current
    var scooterData by remember { mutableStateOf(ScooterData()) }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val receivedData = intent?.serializable<ScooterData>("data")
            receivedData?.let { scooterData = it }
        }
    }

    LaunchedEffect(device) {
        val intent = Intent(context, BluetoothForegroundService::class.java).apply {
            putExtra("device", device)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        context.registerReceiver(
            receiver,
            IntentFilter("BluetoothData"),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_EXPORTED else 0
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            disposeService(context, receiver)
        }
    }

    Navigator(
        MainDashboardTab
    ) { navigator ->
        CompositionLocalProvider(
            LocalScooterStatus provides scooterData
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        actions = {
                            IconButton(
                                onClick = {disposeService(context, receiver); onClose.invoke() }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null
                                )
                            }
                        },
                        title = {
                            Text(text = "$name: ${scooterData.isConnected}")
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        TabNavigationItem(MainDashboardTab, ImageVector.vectorResource(R.drawable.scooter))
                        TabNavigationItem(SettingsTab, Icons.Rounded.Build)
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    FadeTransition(navigator)
                }
            }
        }
    }
}


fun disposeService(context: Context, receiver: BroadcastReceiver) {
    try {
        context.unregisterReceiver(receiver)
    } catch (e: IllegalArgumentException) {
        Log.e("MainScreen", "fuck something went 50\\50 wrong")
    }

    val stopIntent = Intent(context, BluetoothForegroundService::class.java)
    context.stopService(stopIntent)
}

