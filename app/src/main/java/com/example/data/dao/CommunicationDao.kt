package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CommunicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunicationDao {
  @Query("SELECT * FROM communications WHERE userCellNumber = :userCellNumber ORDER BY timestamp DESC")
  fun getAllForUser(userCellNumber: String): Flow<List<CommunicationEntity>>

  @Query("SELECT * FROM communications WHERE userCellNumber = :userCellNumber AND type LIKE 'CALL%' ORDER BY timestamp DESC")
  fun getCallsForUser(userCellNumber: String): Flow<List<CommunicationEntity>>

  @Query("SELECT * FROM communications WHERE userCellNumber = :userCellNumber AND (type LIKE 'SMS%' OR type LIKE 'MESSENGER%') ORDER BY timestamp DESC")
  fun getMessagesForUser(userCellNumber: String): Flow<List<CommunicationEntity>>

  @Query("SELECT * FROM communications WHERE userCellNumber = :userCellNumber AND isRead = 0")
  fun getUnreadForUser(userCellNumber: String): Flow<List<CommunicationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(communication: CommunicationEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(communications: List<CommunicationEntity>)

  @Update
  suspend fun update(communication: CommunicationEntity)

  @Query("UPDATE communications SET isRead = 1 WHERE userCellNumber = :userCellNumber")
  suspend fun markAllAsRead(userCellNumber: String)

  @Query("UPDATE communications SET isRead = 1 WHERE id = :id")
  suspend fun markAsRead(id: Long)

  @Delete
  suspend fun delete(communication: CommunicationEntity)

  @Query("DELETE FROM communications WHERE userCellNumber = :userCellNumber")
  suspend fun clearForUser(userCellNumber: String)
}
