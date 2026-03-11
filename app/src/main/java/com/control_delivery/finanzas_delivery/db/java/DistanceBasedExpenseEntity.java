package com.control_delivery.finanzas_delivery.db.java;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "distance_based_expenses")
public class DistanceBasedExpenseEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    
    @Nullable
    public String description;
    
    @Nullable
    public String typeJson; // Sealed class JSON
    
    public boolean isDeleted;
}
