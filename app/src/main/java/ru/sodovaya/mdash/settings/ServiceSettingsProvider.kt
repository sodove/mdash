package ru.sodovaya.mdash.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import ru.sodovaya.mdash.service.ServiceSettings

@Composable
fun ServiceSettingsProvider(context: Context, content: @Composable () -> Unit) {
    val settingsState = remember {
        val preferences = ServiceSettingsPreferences(
            context = context, settingsState = ServiceSettingsState(ServiceSettings(), null)
        )
        ServiceSettingsState(preferences.loadServiceSettings(), preferences)
    }

    CompositionLocalProvider(LocalServiceSettingsState provides settingsState) {
        content()
    }
}