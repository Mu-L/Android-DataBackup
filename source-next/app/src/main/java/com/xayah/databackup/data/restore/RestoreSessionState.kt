package com.xayah.databackup.data.restore

import com.xayah.databackup.data.rustic.RusticRestoreInventory
import com.xayah.databackup.data.rustic.RusticSnapshot
import com.xayah.databackup.data.rustic.RusticSourceCategory
import com.xayah.databackup.entity.BackupConfig

data class RestoreSessionState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val config: BackupConfig? = null,
    val snapshot: RusticSnapshot? = null,
    val inventory: RusticRestoreInventory? = null,
    val selected: Set<String> = emptySet(),
    val appParts: Map<String, Set<RusticSourceCategory>> = emptyMap(),
)

