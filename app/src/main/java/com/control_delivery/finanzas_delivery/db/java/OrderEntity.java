package com.control_delivery.finanzas_delivery.db.java;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "orders",
    foreignKeys = @ForeignKey(
        entity = TripEntity.class,
        parentColumns = "id",
        childColumns = "tripId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("tripId")}
)
public class OrderEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    
    @Nullable
    public String tripId;
    
    @Nullable
    public String platform;
    
    @Nullable
    public String customerAddress;
    
    public long totalAmount;
    
    @Nullable
    public String status;
    
    public long timestamp;
    
    @Nullable
    public String pickupLocJson;
    
    @Nullable
    public String deliveryLocJson;
    
    public boolean isDeleted;
}
