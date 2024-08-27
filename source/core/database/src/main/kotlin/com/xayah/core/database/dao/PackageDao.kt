package com.xayah.core.database.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Upsert
import com.xayah.core.model.CompressionType
import com.xayah.core.model.OpType
import com.xayah.core.model.database.PackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {
    @Upsert(entity = PackageEntity::class)
    suspend fun upsert(items: List<PackageEntity>)

    @Upsert(entity = PackageEntity::class)
    suspend fun upsert(item: PackageEntity)

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_packageName = :packageName AND indexInfo_opType = :opType AND indexInfo_userId = :userId AND indexInfo_preserveId = :preserveId" +
                " AND indexInfo_cloud = :cloud AND indexInfo_backupDir = :backupDir" +
                " LIMIT 1"
    )
    suspend fun query(packageName: String, opType: OpType, userId: Int, preserveId: Long, cloud: String, backupDir: String): PackageEntity?

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_packageName = :packageName AND indexInfo_opType = :opType AND indexInfo_userId = :userId" +
                " LIMIT 1"
    )
    suspend fun query(packageName: String, opType: OpType, userId: Int): PackageEntity?

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_packageName = :packageName AND indexInfo_opType = :opType AND indexInfo_userId = :userId" +
                " AND indexInfo_cloud = :cloud AND indexInfo_backupDir = :backupDir"
    )
    suspend fun query(packageName: String, opType: OpType, userId: Int, cloud: String, backupDir: String): List<PackageEntity>

    @Query(
        "SELECT indexInfo_packageName FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND indexInfo_userId = :userId" +
                " AND extraInfo_blocked = 0 AND extraInfo_existed = 1"
    )
    suspend fun queryPackageNamesByUserId(opType: OpType, userId: Int): List<String>

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_packageName = :packageName AND indexInfo_opType = :opType AND indexInfo_userId = :userId AND indexInfo_preserveId = :preserveId AND indexInfo_compressionType = :ct" +
                " AND indexInfo_cloud = :cloud AND indexInfo_backupDir = :backupDir" +
                " LIMIT 1"
    )
    suspend fun query(packageName: String, opType: OpType, userId: Int, preserveId: Long, ct: CompressionType, cloud: String, backupDir: String): PackageEntity?

    @Query("SELECT * FROM PackageEntity WHERE extraInfo_activated = 1 AND extraInfo_existed = 1 AND indexInfo_opType = :opType")
    suspend fun queryActivated(opType: OpType): List<PackageEntity>

    @Query("SELECT * FROM PackageEntity WHERE extraInfo_activated = 1 AND extraInfo_existed = 1 AND indexInfo_opType = :opType AND indexInfo_cloud = :cloud AND indexInfo_backupDir = :backupDir")
    suspend fun queryActivated(opType: OpType, cloud: String, backupDir: String): List<PackageEntity>

    @Query("UPDATE PackageEntity SET extraInfo_activated = 0 WHERE indexInfo_opType = :opType")
    suspend fun clearActivated(opType: OpType)

    @Query("UPDATE PackageEntity SET extraInfo_existed = 0 WHERE indexInfo_opType = :opType")
    suspend fun clearExisted(opType: OpType)

    @Query("UPDATE PackageEntity SET extraInfo_activated = :activated WHERE id = :id")
    suspend fun activateById(id: Long, activated: Boolean)

    @Query("UPDATE PackageEntity SET extraInfo_activated = :activated WHERE id in (:ids)")
    suspend fun activateByIds(ids: List<Long>, activated: Boolean)

    @Query("UPDATE PackageEntity SET extraInfo_activated = NOT extraInfo_activated WHERE id in (:ids)")
    suspend fun reverseActivatedByIds(ids: List<Long>)

    @Query(
        "SELECT id FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND extraInfo_existed = :existed AND extraInfo_blocked = :blocked AND indexInfo_userId = :userId"
    )
    suspend fun queryAppIds(opType: OpType, userId: Int, existed: Boolean, blocked: Boolean): List<Long>

    @Query(
        "UPDATE PackageEntity" +
                " SET dataStates_apkState = :apk," +
                " dataStates_userState = :user," +
                " dataStates_userDeState = :userDe," +
                " dataStates_dataState = :data," +
                " dataStates_obbState = :obb," +
                " dataStates_mediaState = :media" +
                " WHERE id = :id"
    )
    suspend fun selectDataItemsById(id: Long, apk: String, user: String, userDe: String, data: String, obb: String, media: String)

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_packageName = :packageName AND indexInfo_opType = :opType AND indexInfo_userId = :userId AND indexInfo_preserveId = :preserveId" +
                " LIMIT 1"
    )
    fun queryFlow(packageName: String, opType: OpType, userId: Int, preserveId: Long): Flow<PackageEntity?>

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND extraInfo_existed = 1"
    )
    fun queryPackagesFlow(opType: OpType): Flow<List<PackageEntity>>

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND extraInfo_existed = :existed AND extraInfo_blocked = :blocked"
    )
    fun queryPackagesFlow(opType: OpType, existed: Boolean, blocked: Boolean): Flow<List<PackageEntity>>

    @Query(
        "SELECT COUNT(*) FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND extraInfo_existed = :existed AND extraInfo_blocked = :blocked"
    )
    fun countPackagesFlow(opType: OpType, existed: Boolean, blocked: Boolean): Flow<Long>

    @Query(
        "SELECT COUNT(*) FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND extraInfo_existed = :existed AND extraInfo_blocked = :blocked AND extraInfo_activated = 1"
    )
    fun countActivatedPackagesFlow(opType: OpType, existed: Boolean, blocked: Boolean): Flow<Long>

    @Query(
        "SELECT indexInfo_userId, COUNT(*) as iCount FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND extraInfo_existed = :existed AND extraInfo_blocked = :blocked AND" +
                " extraInfo_activated = 1 GROUP BY indexInfo_userId"
    )
    fun countUsersMapFlow(opType: OpType, existed: Boolean, blocked: Boolean): Flow<
            Map<@MapColumn(columnName = "indexInfo_userId") Int, @MapColumn(columnName = "iCount") Long>
            >

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND extraInfo_blocked = :blocked"
    )
    fun queryPackagesFlow(opType: OpType, blocked: Boolean): Flow<List<PackageEntity>>

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND extraInfo_existed = 1 AND indexInfo_cloud = :cloud AND indexInfo_backupDir = :backupDir"
    )
    fun queryPackagesFlow(opType: OpType, cloud: String, backupDir: String): Flow<List<PackageEntity>>

    @Query(
        "SELECT DISTINCT indexInfo_userId FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType"
    )
    suspend fun queryUserIds(opType: OpType): List<Int>

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND extraInfo_existed = 1 AND indexInfo_cloud = :cloud AND indexInfo_backupDir = :backupDir"
    )
    suspend fun queryPackages(opType: OpType, cloud: String, backupDir: String): List<PackageEntity>

    @Query(
        "UPDATE PackageEntity" +
                " SET extraInfo_blocked = :blocked" +
                " WHERE id = :id"
    )
    suspend fun setBlocked(id: Long, blocked: Boolean)

    @Query(
        "UPDATE PackageEntity" +
                " SET extraInfo_existed = :existed" +
                " WHERE indexInfo_opType = :opType AND indexInfo_packageName = :packageName AND indexInfo_userId = :userId"
    )
    suspend fun setExisted(opType: OpType, packageName: String, userId: Int, existed: Boolean)

    @Query(
        "UPDATE PackageEntity SET extraInfo_blocked = 0"
    )
    suspend fun clearBlocked()

    @Query("DELETE FROM PackageEntity WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM PackageEntity WHERE indexInfo_backupDir = :backupDir")
    suspend fun delete(backupDir: String)

    @Query(
        "SELECT * FROM PackageEntity WHERE" +
                " indexInfo_opType = :opType AND extraInfo_existed = :existed AND extraInfo_blocked = :blocked"
    )
    fun getApps(opType: OpType, existed: Boolean, blocked: Boolean): Flow<List<PackageEntity>>
}
