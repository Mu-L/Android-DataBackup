package com.xayah.databackup.data.rustic

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import org.junit.Assert.assertEquals
import org.junit.Test

class RusticSnapshotTest {
    @Test
    fun createBackupTagsIncludesBackupMarkerAndConfigUuid() {
        val tags = RusticSnapshot.createBackupTags("config-uuid")
        assertEquals(listOf("databackup", "databackup:config:config-uuid"), tags)
    }

    @Test
    fun snapshotTagsRoundTripThroughJson() {
        val tags = listOf("first-tag", "second-tag")
        val adapter = Moshi.Builder().build().adapter<RusticSnapshot>()
        val snapshot = requireNotNull(adapter.fromJson(adapter.toJson(RusticSnapshot(tags = tags)))) {
            "Snapshot JSON must deserialize to a non-null snapshot."
        }
        assertEquals(tags, snapshot.tags)
    }
}
