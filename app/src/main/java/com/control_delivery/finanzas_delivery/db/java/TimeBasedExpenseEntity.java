package com.control_delivery.finanzas_delivery.db.java;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "time_based_expenses")
public class TimeBasedExpenseEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    
    @Nullable
    public String description;
    
    public long amount;
    public long accumulatedAmount;
    
    @Nullable
    public String frequencyJson; // Sealed class JSON
    
    public long startTimestamp;
    public boolean isDeleted;
    public long nextDeadline;
    public long currentCycleStart;
    public long contributionToday;
    
    @Nullable
    public Long lastContributionTimestamp;
}
