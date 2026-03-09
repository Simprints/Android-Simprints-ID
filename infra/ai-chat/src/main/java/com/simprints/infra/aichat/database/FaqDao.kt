package com.simprints.infra.aichat.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface FaqDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<FaqEntryEntity>)

    @Query("SELECT * FROM FaqEntry WHERE keywords LIKE '%' || :keyword || '%' ORDER BY id LIMIT 5")
    suspend fun search(keyword: String): List<FaqEntryEntity>

    @Query("SELECT * FROM FaqEntry WHERE category = :category ORDER BY id")
    suspend fun getByCategory(category: String): List<FaqEntryEntity>

    @Query("SELECT DISTINCT category FROM FaqEntry ORDER BY category")
    suspend fun getCategories(): List<String>

    @Query("SELECT COUNT(*) FROM FaqEntry")
    suspend fun count(): Int

    @Query("DELETE FROM FaqEntry")
    suspend fun deleteAll()
}
