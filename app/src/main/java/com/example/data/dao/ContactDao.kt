package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
  @Query("SELECT * FROM contacts WHERE userCellNumber = :userCellNumber ORDER BY name ASC")
  fun getContactsForUser(userCellNumber: String): Flow<List<ContactEntity>>

  @Query("SELECT * FROM contacts WHERE userCellNumber = :userCellNumber AND phoneNumber = :phoneNumber LIMIT 1")
  suspend fun findByPhone(userCellNumber: String, phoneNumber: String): ContactEntity?

  @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
  suspend fun getById(id: Long): ContactEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(contact: ContactEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(contacts: List<ContactEntity>)

  @Update
  suspend fun update(contact: ContactEntity)

  @Delete
  suspend fun delete(contact: ContactEntity)

  @Query("DELETE FROM contacts WHERE userCellNumber = :userCellNumber")
  suspend fun clearForUser(userCellNumber: String)
}
