package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CashFlowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashFlowDao {
  @Query("SELECT * FROM cash_flows WHERE userCellNumber = :userCellNumber ORDER BY timestamp DESC")
  fun getAllForUser(userCellNumber: String): Flow<List<CashFlowEntity>>

  @Query("SELECT * FROM cash_flows WHERE userCellNumber = :userCellNumber AND type = 'INCOME' ORDER BY timestamp DESC")
  fun getIncomesForUser(userCellNumber: String): Flow<List<CashFlowEntity>>

  @Query("SELECT * FROM cash_flows WHERE userCellNumber = :userCellNumber AND type = 'EXPENSE' ORDER BY timestamp DESC")
  fun getExpensesForUser(userCellNumber: String): Flow<List<CashFlowEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(cashFlow: CashFlowEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(cashFlows: List<CashFlowEntity>)

  @Update
  suspend fun update(cashFlow: CashFlowEntity)

  @Delete
  suspend fun delete(cashFlow: CashFlowEntity)

  @Query("DELETE FROM cash_flows WHERE userCellNumber = :userCellNumber")
  suspend fun clearForUser(userCellNumber: String)
}
