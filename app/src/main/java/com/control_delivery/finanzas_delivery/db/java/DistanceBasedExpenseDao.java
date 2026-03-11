package com.control_delivery.finanzas_delivery.db.java;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import kotlinx.coroutines.flow.Flow;

@Dao
public interface DistanceBasedExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DistanceBasedExpenseEntity e);

    @Update
    int update(DistanceBasedExpenseEntity e);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DistanceBasedExpenseEntity> list);

    @Query("SELECT * FROM distance_based_expenses WHERE isDeleted = 0")
    Flow<List<DistanceBasedExpenseEntity>> getAll();

    @Query("SELECT * FROM distance_based_expenses WHERE id = :id")
    Flow<DistanceBasedExpenseEntity> getById(String id);

    @Query("UPDATE distance_based_expenses SET isDeleted = 1 WHERE id = :id")
    int softDelete(String id);
}
