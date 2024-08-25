package com.xayah.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.xayah.core.model.DEFAULT_LOAD_SYSTEM_APPS
import com.xayah.core.model.SettingsData
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val KeyLoadSystemApps = booleanPreferencesKey("load_system_apps")

class DbPreferencesDataSource @Inject constructor(
    private val preferences: DataStore<Preferences>
) {
    val settingsData = preferences.data.map {
        SettingsData(
            isLoadSystemApps = it[KeyLoadSystemApps] ?: DEFAULT_LOAD_SYSTEM_APPS
        )
    }

    suspend fun <T> edit(key: Preferences.Key<T>, value: T) {
        preferences.edit { settings -> settings[key] = value }
    }
}
