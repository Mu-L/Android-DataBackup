package com.xayah.databackup.data.restore

import com.xayah.databackup.data.rustic.RusticSourceCategory

enum class RestoreCategory { Apps, Files, Networks, Contacts, CallLogs, Messages }

val AppRestoreParts = setOf(
    RusticSourceCategory.Apk,
    RusticSourceCategory.InternalData,
    RusticSourceCategory.ExternalData,
    RusticSourceCategory.AdditionalData,
)
