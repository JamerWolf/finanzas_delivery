package com.control_delivery.finanzas_delivery.db.java;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TripEntity.class, OrderEntity.class, TimeBasedExpenseEntity.class, DistanceBasedExpenseEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TripDao tripDao();
    public abstract OrderDao orderDao();
    public abstract TimeBasedExpenseDao timeBasedExpenseDao();
    public abstract DistanceBasedExpenseDao distanceBasedExpenseDao();
}
