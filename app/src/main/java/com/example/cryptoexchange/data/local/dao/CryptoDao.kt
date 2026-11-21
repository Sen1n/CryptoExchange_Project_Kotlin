package com.example.cryptoexchange.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cryptoexchange.data.local.entity.CryptoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDao {

    @Query("SELECT * FROM cryptocurrencies ORDER BY rank ASC")
    fun observeAllSortedByRank(): Flow<List<CryptoEntity>>

    @Query("SELECT id FROM cryptocurrencies WHERE is_favorite = 1")
    suspend fun getFavoriteIds(): List<Int>

    @Query("UPDATE cryptocurrencies SET is_favorite = NOT is_favorite WHERE id = :cryptoId")
    suspend fun toggleFavorite(cryptoId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CryptoEntity>)

    @Query("DELETE FROM cryptocurrencies")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceTopList(items: List<CryptoEntity>) {
        val favoriteIds = getFavoriteIds().toSet()
        val updatedItems = items.map { entity ->
            entity.copy(isFavorite = entity.id in favoriteIds)
        }
        deleteAll()
        insertAll(updatedItems)
    }
}