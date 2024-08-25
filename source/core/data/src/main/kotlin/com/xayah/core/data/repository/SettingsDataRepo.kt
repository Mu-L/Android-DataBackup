package com.xayah.core.data.repository

import com.xayah.core.datastore.DbPreferencesDataSource
import com.xayah.core.datastore.KeyLoadSystemApps
import com.xayah.core.model.SettingsData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsDataRepo @Inject constructor(
    private val dbPreferencesDataSource: DbPreferencesDataSource,
) {
    val settingsData: Flow<SettingsData> = dbPreferencesDataSource.settingsData

    suspend fun setLoadSystemApps(value: Boolean) {
        dbPreferencesDataSource.edit(KeyLoadSystemApps, value)
    }
}
