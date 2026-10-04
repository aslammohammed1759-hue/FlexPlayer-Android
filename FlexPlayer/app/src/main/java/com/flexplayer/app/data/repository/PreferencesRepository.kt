package com.flexplayer.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flexplayer.app.model.ConversionPreset
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("flexplayer_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_PRESET = stringPreferencesKey("default_preset")

    val defaultPreset: Flow<ConversionPreset> = context.dataStore.data.map { prefs ->
        prefs[KEY_PRESET]?.let { name ->
            runCatching { ConversionPreset.valueOf(name) }.getOrNull()
        } ?: ConversionPreset.BALANCED
    }

    suspend fun setDefaultPreset(preset: ConversionPreset) {
        context.dataStore.edit { it[KEY_PRESET] = preset.name }
    }
}
