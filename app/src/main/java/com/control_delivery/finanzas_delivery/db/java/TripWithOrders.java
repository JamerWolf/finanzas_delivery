package com.control_delivery.finanzas_delivery.db.java;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

public class TripWithOrders {
    @Embedded
    public TripEntity trip;

    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    public List<OrderEntity> orders;
}
