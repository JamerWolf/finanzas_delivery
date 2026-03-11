package com.control_delivery.finanzas_delivery.db.java;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import kotlinx.coroutines.flow.Flow;

@Dao
public interface TimeBasedExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TimeBasedExpenseEntity e);

    @Update
    int update(TimeBasedExpenseEntity e);

    @Query("SELECT * FROM time_based_expenses WHERE isDeleted = 0")
    Flow<List<TimeBasedExpenseEntity>> getAll();

    @Query("SELECT * FROM time_based_expenses WHERE id = :id")
    Flow<TimeBasedExpenseEntity> getById(String id);

    @Query("UPDATE time_based_expenses SET isDeleted = 1 WHERE id = :id")
    int softDelete(String id);
}
