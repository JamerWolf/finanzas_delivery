package com.control_delivery.finanzas_delivery.db.java;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import kotlinx.coroutines.flow.Flow;

@Dao
public interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrder(OrderEntity order);

    @Update
    int updateOrder(OrderEntity order);

    @Query("SELECT * FROM orders WHERE id = :id AND isDeleted = 0")
    Flow<OrderEntity> getOrderById(String id);
    
    @Query("SELECT * FROM orders WHERE tripId = :tripId AND isDeleted = 0")
    Flow<List<OrderEntity>> getOrdersByTripId(String tripId);

    @Query("UPDATE orders SET isDeleted = 1 WHERE id = :id")
    int softDeleteOrder(String id);
}
