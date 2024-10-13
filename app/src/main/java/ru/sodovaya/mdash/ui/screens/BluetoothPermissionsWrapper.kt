package ru.sodovaya.mdash.ui.screens

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import ru.sodovaya.mdash.composables.PermissionBox


@Composable
fun BluetoothPermissionsWrapper(
    extraPermissions: Set<String> = emptySet(),
    content: @Composable BoxScope.(BluetoothAdapter) -> Unit,
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val bluetoothAdapter = context.getSystemService<BluetoothManager>()?.adapter

    val locationPermission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        setOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    } else {
        emptySet()
    }

    val bluetoothPermissionSet = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else {
        setOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
    }

    val notificationsPermissionSet = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        setOf(Manifest.permission.POST_NOTIFICATIONS)
    } else { setOf() }

    PermissionBox(
        permissions = (bluetoothPermissionSet + locationPermission +
                notificationsPermissionSet + extraPermissions).toList(),
        contentAlignment = Alignment.Center,
    ) {
        val hasBT = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        val hasBLE = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        var isBTEnabled by remember {
            mutableStateOf(bluetoothAdapter?.isEnabled == true)
        }

        when {
            bluetoothAdapter == null || !hasBT -> MissingFeatureText(text = "No bluetooth available")
            !hasBLE -> MissingFeatureText(text = "No bluetooth low energy available")
            !isBTEnabled -> BluetoothDisabledScreen { isBTEnabled = true }
            else -> content(bluetoothAdapter)
        }
    }
}

@Composable
fun BluetoothDisabledScreen(onEnabled: () -> Unit) {
    val result =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                onEnabled()
            }
        }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Bluetooth is disabled")
        Button(
            onClick = {
                result.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            },
        ) {
            Text(text = "Enable")
        }
    }
}

@Composable
private fun MissingFeatureText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.error,
    )
}
