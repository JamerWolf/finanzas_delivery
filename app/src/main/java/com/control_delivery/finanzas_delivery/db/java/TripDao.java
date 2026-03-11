package com.control_delivery.finanzas_delivery.db.java;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import java.util.List;
import kotlinx.coroutines.flow.Flow;

@Dao
public interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTrip(TripEntity trip);

    @Update
    int updateTrip(TripEntity trip);

    @Transaction
    @Query("SELECT * FROM trips WHERE isDeleted = 0 ORDER BY startTimestamp DESC")
    Flow<List<TripWithOrders>> getAllTrips();
    
    @Transaction
    @Query("SELECT * FROM trips WHERE id = :id AND isDeleted = 0")
    Flow<TripWithOrders> getTripById(String id);

    @Transaction
    @Query("SELECT * FROM trips WHERE id IN (SELECT tripId FROM orders WHERE id = :orderId) AND isDeleted = 0")
    Flow<TripWithOrders> getTripByOrderId(String orderId);

    @Transaction
    @Query("SELECT * FROM trips WHERE startTimestamp BETWEEN :startDate AND :endDate AND isDeleted = 0 ORDER BY startTimestamp DESC")
    Flow<List<TripWithOrders>> getTripsInRange(long startDate, long endDate);

    @Query("UPDATE trips SET isDeleted = 1 WHERE id = :id")
    int softDeleteTrip(String id);
}
