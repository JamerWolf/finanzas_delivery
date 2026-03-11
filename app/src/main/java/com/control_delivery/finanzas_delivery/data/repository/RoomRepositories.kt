package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.data.json.GsonFactory
import com.control_delivery.finanzas_delivery.db.java.*
import com.control_delivery.finanzas_delivery.domain.model.*
import com.control_delivery.finanzas_delivery.domain.repository.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class RoomTripRepository @Inject constructor(private val tripDao: TripDao, private val orderDao: OrderDao) : TripRepository {
    private val gson = GsonFactory.gson
    private val routeType = object : TypeToken<List<RoutePoint>>() {}.type
    private val mapType = object : TypeToken<Map<String, Long>>() {}.type

    private fun OrderEntity.toDomain() = Order(
        id = id, 
        platform = platform ?: "", 
        customerAddress = customerAddress ?: "", 
        totalAmount = totalAmount, 
        status = OrderStatus.valueOf(status ?: "ON_THE_WAY_TO_RECEIVE"), 
        timestamp = timestamp, 
        pickupLocation = pickupLocJson?.let { gson.fromJson(it, RoutePoint::class.java) }, 
        deliveryLocation = deliveryLocJson?.let { gson.fromJson(it, RoutePoint::class.java) }, 
        isDeleted = isDeleted
    )

    private fun Order.toEntity(tid: String) = OrderEntity().apply { 
        id = this@toEntity.id
        tripId = tid
        platform = this@toEntity.platform
        customerAddress = this@toEntity.customerAddress
        totalAmount = this@toEntity.totalAmount
        status = this@toEntity.status.name
        timestamp = this@toEntity.timestamp
        pickupLocJson = pickupLocation?.let { gson.toJson(it) }
        deliveryLocJson = deliveryLocation?.let { gson.toJson(it) }
        isDeleted = this@toEntity.isDeleted 
    }
    
    private fun TripWithOrders.toDomain(): Trip {
        val orders = this.orders.map { it.toDomain() }
        return Trip(
            id = trip.id, 
            orders = orders, 
            status = TripStatus.valueOf(trip.status ?: "ACTIVE"), 
            totalDistanceKm = trip.totalDistanceKm, 
            startTimestamp = trip.startTimestamp, 
            endTimestamp = trip.endTimestamp, 
            route = trip.routeJson?.let { gson.fromJson(it, routeType) } ?: emptyList(), 
            snappedRoute = trip.snappedRouteJson?.let { gson.fromJson(it, routeType) },
            kmDeduction = trip.kmDeduction, 
            timeExpensesDeduction = trip.timeExpensesDeduction, 
            kmDeductionsBreakdown = trip.kmBreakdownJson?.let { gson.fromJson(it, mapType) } ?: emptyMap(), 
            timeExpensesDeductionsBreakdown = trip.timeBreakdownJson?.let { gson.fromJson(it, mapType) } ?: emptyMap(), 
            isDeleted = trip.isDeleted
        )
    }
    
    private fun Trip.toEntity() = TripEntity().apply { 
        id = this@toEntity.id
        status = this@toEntity.status.name
        totalDistanceKm = this@toEntity.totalDistanceKm
        startTimestamp = this@toEntity.startTimestamp
        endTimestamp = this@toEntity.endTimestamp
        routeJson = gson.toJson(route)
        snappedRouteJson = snappedRoute?.let { gson.toJson(it) }
        kmDeduction = this@toEntity.kmDeduction
        timeExpensesDeduction = this@toEntity.timeExpensesDeduction
        kmBreakdownJson = gson.toJson(kmDeductionsBreakdown)
        timeBreakdownJson = gson.toJson(timeExpensesDeductionsBreakdown)
        isDeleted = this@toEntity.isDeleted 
    }

    override fun getTripById(id: String) = tripDao.getTripById(id).map { it?.toDomain() }
    
    override fun getTripByOrderId(orderId: String) = tripDao.getTripByOrderId(orderId).map { it?.toDomain() }

    override fun getTripsInDateRange(startDate: Long, endDate: Long) = 
        tripDao.getTripsInRange(startDate, endDate).map { list ->
            list.map { it.toDomain() }
        }

    override fun getCompletedTripsInDateRange(startDate: Long, endDate: Long) = getTripsInDateRange(startDate, endDate).map { list ->
        list.filter { it.status == TripStatus.COMPLETED }
    }

    override fun getTripsTotalAmountAfterKm(startDate: Long, endDate: Long) = getCompletedTripsInDateRange(startDate, endDate).map { list ->
        list.sumOf { it.amountAfterKmDeduction }
    }
    
    override suspend fun addTrip(trip: Trip): String = withContext(Dispatchers.IO) {
        tripDao.insertTrip(trip.toEntity())
        trip.orders.forEach { order ->
            orderDao.insertOrder(order.toEntity(trip.id))
        }
        trip.id
    }
    
    override suspend fun updateTrip(trip: Trip) = withContext(Dispatchers.IO) {
        tripDao.updateTrip(trip.toEntity())
        trip.orders.forEach { order ->
            orderDao.insertOrder(order.toEntity(trip.id))
        }
    }
    
    override suspend fun deleteTrip(id: String) = withContext(Dispatchers.IO) {
        tripDao.softDeleteTrip(id)
        Unit
    }
}

@Singleton
class RoomOrderRepository @Inject constructor(private val orderDao: OrderDao, private val tripDao: TripDao) : OrderRepository {
    private val gson = GsonFactory.gson
    private fun OrderEntity.toDomain() = Order(
        id = id, 
        platform = platform ?: "", 
        customerAddress = customerAddress ?: "", 
        totalAmount = totalAmount, 
        status = OrderStatus.valueOf(status ?: "ON_THE_WAY_TO_RECEIVE"), 
        timestamp = timestamp, 
        pickupLocation = pickupLocJson?.let { gson.fromJson(it, RoutePoint::class.java) }, 
        deliveryLocation = deliveryLocJson?.let { gson.fromJson(it, RoutePoint::class.java) }, 
        isDeleted = isDeleted
    )

    private fun Order.toEntity(tid: String) = OrderEntity().apply { 
        id = this@toEntity.id
        tripId = tid
        platform = this@toEntity.platform
        customerAddress = this@toEntity.customerAddress
        totalAmount = this@toEntity.totalAmount
        status = this@toEntity.status.name
        timestamp = this@toEntity.timestamp
        pickupLocJson = pickupLocation?.let { gson.toJson(it) }
        deliveryLocJson = deliveryLocation?.let { gson.toJson(it) }
        isDeleted = this@toEntity.isDeleted 
    }

    override fun getOrderById(id: String) = orderDao.getOrderById(id).map { it?.toDomain() }
    
    override suspend fun addOrder(order: Order): String = withContext(Dispatchers.IO) {
        order.id
    }
    
    override fun getOrdersTotalAmount(startDate: Long, endDate: Long) = flowOf(0L)
    override fun getOrdersByStatesInDateRange(status: List<OrderStatus>, startDate: Long, endDate: Long) = flowOf(emptyList<Order>())
    
    override suspend fun deleteOrder(id: String) = withContext(Dispatchers.IO) { 
        orderDao.softDeleteOrder(id)
        Unit
    }
    
    override suspend fun updateOrder(order: Order) = withContext(Dispatchers.IO) {
        val existing = orderDao.getOrderById(order.id).first()
        if (existing != null) orderDao.updateOrder(order.toEntity(existing.tripId ?: ""))
        Unit
    }
}

@Singleton
class RoomTimeBasedExpenseRepository @Inject constructor(private val dao: TimeBasedExpenseDao) : TimeBasedExpenseRepository {
    private val gson = GsonFactory.gson
    private fun TimeBasedExpenseEntity.toDomain() = TimeBasedExpense(
        id = id, 
        description = description ?: "", 
        amount = amount, 
        accumulatedAmount = accumulatedAmount, 
        frequency = frequencyJson?.let { gson.fromJson(it, ExpenseFrequency::class.java) } ?: ExpenseFrequency.Daily, 
        startTimestamp = startTimestamp, 
        isDeleted = isDeleted, 
        nextDeadline = nextDeadline, 
        currentCycleStart = currentCycleStart, 
        contributionToday = contributionToday, 
        lastContributionTimestamp = lastContributionTimestamp
    )

    private fun TimeBasedExpense.toEntity() = TimeBasedExpenseEntity().apply { 
        id = this@toEntity.id
        description = this@toEntity.description
        amount = this@toEntity.amount
        accumulatedAmount = this@toEntity.accumulatedAmount
        frequencyJson = gson.toJson(frequency)
        startTimestamp = this@toEntity.startTimestamp
        isDeleted = this@toEntity.isDeleted
        nextDeadline = this@toEntity.nextDeadline
        currentCycleStart = this@toEntity.currentCycleStart
        contributionToday = this@toEntity.contributionToday
        lastContributionTimestamp = this@toEntity.lastContributionTimestamp
    }

    override fun getAllExpenses() = dao.getAll().map { l -> l.map { it.toDomain() } }
    override fun getExpenseById(id: String) = dao.getById(id).map { it?.toDomain() }
    override suspend fun addExpense(expense: TimeBasedExpense) = withContext(Dispatchers.IO) { 
        dao.insert(expense.toEntity())
        Unit
    }
    override suspend fun updateExpenses(expenses: List<TimeBasedExpense>) = withContext(Dispatchers.IO) { 
        expenses.forEach { dao.update(it.toEntity()) } 
    }
    override suspend fun deleteExpense(id: String) = withContext(Dispatchers.IO) { 
        dao.softDelete(id)
        Unit
    }
    
    override suspend fun subtractContribution(id: String, amount: Long, contributionDate: LocalDate) = withContext(Dispatchers.IO) {
        val e = dao.getById(id).first()?.toDomain() ?: return@withContext
        val newAcc = (e.accumulatedAmount - amount).coerceAtLeast(0)
        val newToday = if (contributionDate == LocalDate.now()) (e.contributionToday - amount).coerceAtLeast(0) else e.contributionToday
        dao.update(e.copy(accumulatedAmount = newAcc, contributionToday = newToday).toEntity())
        Unit
    }
    
    override suspend fun syncExpenses(today: LocalDate) = withContext(Dispatchers.IO) {
        val all = dao.getAll().first()
        all.forEach { 
            val synced = it.toDomain().syncDailyContribution(today)
            if (synced != it.toDomain()) dao.update(synced.toEntity())
        }
    }
}

@Singleton
class RoomDistanceBasedExpenseRepository @Inject constructor(private val dao: DistanceBasedExpenseDao) : DistanceBasedExpenseRepository {
    private val gson = GsonFactory.gson
    private fun DistanceBasedExpenseEntity.toDomain() = DistanceBasedExpense(
        id = id, 
        description = description ?: "", 
        type = gson.fromJson(typeJson ?: "{}", DistanceExpenseType::class.java) ?: DistanceExpenseType.PureDeduction(0, 0.0), 
        isDeleted = isDeleted
    )

    private fun DistanceBasedExpense.toEntity() = DistanceBasedExpenseEntity().apply { 
        id = this@toEntity.id
        description = this@toEntity.description
        typeJson = gson.toJson(type)
        isDeleted = this@toEntity.isDeleted 
    }

    override fun getDistanceBasedExpenses() = dao.getAll().map { l -> l.map { it.toDomain() } }
    override suspend fun saveExpense(expense: DistanceBasedExpense) = withContext(Dispatchers.IO) { 
        dao.insert(expense.toEntity())
        Unit
    }
    override suspend fun deleteExpense(id: String) = withContext(Dispatchers.IO) { 
        dao.softDelete(id)
        Unit
    }
    override suspend fun updateExpenses(expenses: List<DistanceBasedExpense>) = withContext(Dispatchers.IO) { 
        dao.insertAll(expenses.map { it.toEntity() }) 
    }
    
    override suspend fun resetExpense(id: String) = withContext(Dispatchers.IO) {
        val e = dao.getById(id).first()?.toDomain() ?: return@withContext
        dao.update(e.reset().toEntity())
        Unit
    }
}
