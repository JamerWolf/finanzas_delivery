package com.control_delivery.finanzas_delivery.db.java;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trips")
public class TripEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    
    @Nullable
    public String status;
    
    public double totalDistanceKm;
    public long startTimestamp;
    
    @Nullable
    public Long endTimestamp;
    
    @Nullable
    public String routeJson;
    
    public long kmDeduction;
    public long timeExpensesDeduction;
    
    @Nullable
    public String kmBreakdownJson;
    
    @Nullable
    public String timeBreakdownJson;
    
    public boolean isDeleted;
}
