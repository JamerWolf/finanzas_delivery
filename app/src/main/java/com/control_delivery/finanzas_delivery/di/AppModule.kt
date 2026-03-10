package com.control_delivery.finanzas_delivery.di

import android.content.Context
import com.control_delivery.finanzas_delivery.data.location.FusedLocationTrackerImpl
import com.control_delivery.finanzas_delivery.data.repository.DistanceBasedExpenseInMemoryRepository
import com.control_delivery.finanzas_delivery.data.repository.OrderInMemoryRepository
import com.control_delivery.finanzas_delivery.data.repository.TimeBasedExpenseInMemoryRepository
import com.control_delivery.finanzas_delivery.data.repository.TripInMemoryRepository
import com.control_delivery.finanzas_delivery.domain.location.LocationTracker
import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import com.control_delivery.finanzas_delivery.domain.repository.TripRepository
import com.control_delivery.finanzas_delivery.domain.usecases.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // --- GPS / LOCATION ---
    @Provides
    @Singleton
    fun provideLocationTracker(
        @ApplicationContext context: Context
    ): LocationTracker {
        return FusedLocationTrackerImpl(context)
    }

    // --- REPOSITORIES ---
    @Provides
    @Singleton
    fun provideOrderRepository(orderInMemoryRepository: OrderInMemoryRepository): OrderRepository {
        return orderInMemoryRepository
    }

    @Provides
    @Singleton
    fun provideOrderInMemoryRepository(): OrderInMemoryRepository {
        return OrderInMemoryRepository()
    }

    @Provides
    @Singleton
    fun provideTimeBasedExpenseInMemoryRepository(): TimeBasedExpenseInMemoryRepository {
        return TimeBasedExpenseInMemoryRepository()
    }

    @Provides
    @Singleton
    fun provideTimeBasedExpenseRepository(
        timeBasedExpenseInMemoryRepository: TimeBasedExpenseInMemoryRepository): TimeBasedExpenseRepository {
        return timeBasedExpenseInMemoryRepository
    }

    @Provides
    @Singleton
    fun provideDistanceBasedExpenseInMemoryRepository(): DistanceBasedExpenseInMemoryRepository {
        return DistanceBasedExpenseInMemoryRepository()
    }

    @Provides
    @Singleton
    fun provideDistanceBasedExpenseRepository(
        distanceBasedExpenseInMemoryRepository: DistanceBasedExpenseInMemoryRepository
    ): DistanceBasedExpenseRepository {
        return distanceBasedExpenseInMemoryRepository
    }

    @Provides
    @Singleton
    fun provideTripInMemoryRepository(): TripInMemoryRepository {
        return TripInMemoryRepository()
    }

    @Provides
    @Singleton
    fun provideTripRepository(
        tripInMemoryRepository: TripInMemoryRepository
    ): TripRepository {
        return tripInMemoryRepository
    }

    // --- USE CASES ---

    @Provides
    @Singleton
    fun provideSyncTimeBasedExpensesUseCase(
        repository: TimeBasedExpenseRepository
    ): SyncTimeBasedExpensesUseCase {
        return SyncTimeBasedExpensesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetTotalTimeBasedExpensesImpactUseCase(
        repository: TimeBasedExpenseRepository
    ): GetTotalTimeBasedExpensesImpactUseCase {
        return GetTotalTimeBasedExpensesImpactUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetOrderByIdUseCase(
        orderRepository: OrderRepository
    ): GetOrderByIdUseCase {
        return GetOrderByIdUseCase(orderRepository)
    }

    @Provides
    @Singleton
    fun provideGetOrdersAmountAfterKmUseCase(
        tripRepository: TripRepository
    ): GetOrdersAmountAfterKmUseCase {
        return GetOrdersAmountAfterKmUseCase(tripRepository)
    }

    @Provides
    @Singleton
    fun provideGetAmountNetUseCase(
        getOrdersAmountAfterKmUseCase: GetOrdersAmountAfterKmUseCase,
        getTotalTimeBasedExpensesImpactUseCase: GetTotalTimeBasedExpensesImpactUseCase
    ): GetAmountNetUseCase {
        return GetAmountNetUseCase(
            getOrdersAmountAfterKmUseCase,
            getTotalTimeBasedExpensesImpactUseCase
        )
    }

    @Provides
    @Singleton
    fun provideAddOrderUseCase(
        orderRepository: OrderRepository
    ): AddOrderUseCase {
        return AddOrderUseCase(
            orderRepository
        )
    }

    @Provides
    @Singleton
    fun provideGetOrdersFlowUseCase(
        orderRepository: OrderRepository
    ): GetOrdersFlowUseCase {
        return GetOrdersFlowUseCase(orderRepository)
    }

    @Provides
    @Singleton
    fun provideGetTimeBasedExpensesUseCase(
        repository: TimeBasedExpenseRepository
    ): GetTimeBasedExpensesUseCase {
        return GetTimeBasedExpensesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetTimeBasedExpenseByIdUseCase(
        repository: TimeBasedExpenseRepository
    ): GetTimeBasedExpenseByIdUseCase {
        return GetTimeBasedExpenseByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddTimeBasedExpenseUseCase(
        repository: TimeBasedExpenseRepository
    ): AddTimeBasedExpenseUseCase {
        return AddTimeBasedExpenseUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteTimeBasedExpenseUseCase(
        repository: TimeBasedExpenseRepository
    ): DeleteTimeBasedExpenseUseCase {
        return DeleteTimeBasedExpenseUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateTimeBasedExpenseUseCase(
        repository: TimeBasedExpenseRepository
    ): UpdateTimeBasedExpenseUseCase {
        return UpdateTimeBasedExpenseUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteOrderUseCase(
        orderRepository: OrderRepository,
        tripRepository: TripRepository,
        processTripIncomeUseCase: ProcessTripIncomeUseCase,
        reverseTripIncomeUseCase: ReverseTripIncomeUseCase
    ): DeleteOrderUseCase {
        return DeleteOrderUseCase(orderRepository, tripRepository, processTripIncomeUseCase, reverseTripIncomeUseCase)
    }

    @Provides
    @Singleton
    fun provideUpdateOrderUseCase(
        orderRepository: OrderRepository,
        tripRepository: TripRepository,
        processTripIncomeUseCase: ProcessTripIncomeUseCase,
        reverseTripIncomeUseCase: ReverseTripIncomeUseCase
    ): UpdateOrderUseCase {
        return UpdateOrderUseCase(orderRepository, tripRepository, processTripIncomeUseCase, reverseTripIncomeUseCase)
    }

    @Provides
    @Singleton
    fun provideGetDistanceBasedExpensesUseCase(
        repository: DistanceBasedExpenseRepository
    ): GetDistanceBasedExpensesUseCase {
        return GetDistanceBasedExpensesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddDistanceBasedExpenseUseCase(
        repository: DistanceBasedExpenseRepository
    ): AddDistanceBasedExpenseUseCase {
        return AddDistanceBasedExpenseUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteDistanceBasedExpenseUseCase(
        repository: DistanceBasedExpenseRepository
    ): DeleteDistanceBasedExpenseUseCase {
        return DeleteDistanceBasedExpenseUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateDistanceBasedExpenseUseCase(
        repository: DistanceBasedExpenseRepository
    ): UpdateDistanceBasedExpenseUseCase {
        return UpdateDistanceBasedExpenseUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideResetDistanceExpenseUseCase(
        repository: DistanceBasedExpenseRepository
    ): ResetDistanceExpenseUseCase {
        return ResetDistanceExpenseUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideApplyKmDeductionUseCase(
        repository: DistanceBasedExpenseRepository
    ): ApplyKmDeductionUseCase = ApplyKmDeductionUseCase(repository)

    @Provides
    @Singleton
    fun provideApplyTimeBasedDeductionUseCase(repo: TimeBasedExpenseRepository): ApplyTimeBasedDeductionUseCase =
        ApplyTimeBasedDeductionUseCase(repo)

    @Provides
    @Singleton
    fun provideProcessTripIncomeUseCase(
        kmFilter: ApplyKmDeductionUseCase,
        timeFilter: ApplyTimeBasedDeductionUseCase
    ): ProcessTripIncomeUseCase = ProcessTripIncomeUseCase(kmFilter, timeFilter)

    @Provides
    @Singleton
    fun provideCompleteTripUseCase(
        activeTripManager: com.control_delivery.finanzas_delivery.domain.trip.ActiveTripManager,
        processTripIncomeUseCase: ProcessTripIncomeUseCase,
        tripRepository: TripRepository,
        orderRepository: OrderRepository
    ): CompleteTripUseCase = CompleteTripUseCase(
        activeTripManager, processTripIncomeUseCase, tripRepository, orderRepository
    )

    @Provides
    @Singleton
    fun provideGetTripsFlowUseCase(
        tripRepository: TripRepository
    ): GetTripsFlowUseCase = GetTripsFlowUseCase(tripRepository)

    @Provides
    @Singleton
    fun provideGetTripByIdUseCase(
        tripRepository: TripRepository
    ): GetTripByIdUseCase = GetTripByIdUseCase(tripRepository)

    @Provides
    @Singleton
    fun provideReverseTripIncomeUseCase(
        timeBasedRepo: TimeBasedExpenseRepository,
        distanceBasedRepo: DistanceBasedExpenseRepository
    ): ReverseTripIncomeUseCase = ReverseTripIncomeUseCase(timeBasedRepo, distanceBasedRepo)

    @Provides
    @Singleton
    fun provideDeleteTripUseCase(
        tripRepository: TripRepository,
        reverseTripIncomeUseCase: ReverseTripIncomeUseCase
    ): DeleteTripUseCase = DeleteTripUseCase(tripRepository, reverseTripIncomeUseCase)

    // --- ROUTING ---
    @Provides
    @Singleton
    fun provideOsrmApiService(): com.control_delivery.finanzas_delivery.data.routing.OsrmApiService {
        val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        
        // OSRM requires a descriptive User-Agent
        val userAgentInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "FinanzasDeliveryApp/1.0 (Android)")
                .build()
            chain.proceed(request)
        }

        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return retrofit2.Retrofit.Builder()
            .baseUrl("http://router.project-osrm.org/")
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(com.control_delivery.finanzas_delivery.data.routing.OsrmApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRouteRepository(
        apiService: com.control_delivery.finanzas_delivery.data.routing.OsrmApiService
    ): com.control_delivery.finanzas_delivery.domain.routing.RouteRepository {
        return com.control_delivery.finanzas_delivery.data.routing.OsrmRouteRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideGetSnappedRouteUseCase(
        routeRepository: com.control_delivery.finanzas_delivery.domain.routing.RouteRepository
    ): com.control_delivery.finanzas_delivery.domain.usecases.GetSnappedRouteUseCase {
        return com.control_delivery.finanzas_delivery.domain.usecases.GetSnappedRouteUseCase(routeRepository)
    }
}
