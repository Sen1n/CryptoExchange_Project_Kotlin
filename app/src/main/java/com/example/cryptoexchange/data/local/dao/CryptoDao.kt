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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CryptoEntity>)

    @Query("DELETE FROM cryptocurrencies")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceTopList(items: List<CryptoEntity>) {
        deleteAll()
        insertAll(items)
    }
}