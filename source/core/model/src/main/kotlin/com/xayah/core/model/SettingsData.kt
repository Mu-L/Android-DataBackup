package com.xayah.core.model

const val DEFAULT_LOAD_SYSTEM_APPS = false

data class SettingsData(
    val isLoadSystemApps: Boolean = DEFAULT_LOAD_SYSTEM_APPS,
)
