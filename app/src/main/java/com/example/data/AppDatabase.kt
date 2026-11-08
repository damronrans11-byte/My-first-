package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CashFlowDao
import com.example.data.dao.CommunicationDao
import com.example.data.dao.ContactDao
import com.example.data.entity.CashFlowEntity
import com.example.data.entity.CommunicationEntity
import com.example.data.entity.ContactEntity

@Database(
  entities = [
    ContactEntity::class,
    CommunicationEntity::class,
    CashFlowEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun contactDao(): ContactDao
  abstract fun communicationDao(): CommunicationDao
  abstract fun cashFlowDao(): CashFlowDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "contact_pulse_db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
