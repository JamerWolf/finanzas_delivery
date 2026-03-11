package com.control_delivery.finanzas_delivery.di

import android.content.Context
import androidx.room.Room
import com.control_delivery.finanzas_delivery.db.java.*
import com.control_delivery.finanzas_delivery.data.location.FusedLocationTrackerImpl
import com.control_delivery.finanzas_delivery.data.repository.*
import com.control_delivery.finanzas_delivery.domain.location.LocationTracker
import com.control_delivery.finanzas_delivery.domain.repository.*
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "finanzas_delivery_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideTripDao(db: AppDatabase) = db.tripDao()
    @Provides fun provideOrderDao(db: AppDatabase) = db.orderDao()
    @Provides fun provideTimeDao(db: AppDatabase) = db.timeBasedExpenseDao()
    @Provides fun provideDistanceDao(db: AppDatabase) = db.distanceBasedExpenseDao()

    @Provides
    @Singleton
    fun provideLocationTracker(@ApplicationContext context: Context): LocationTracker {
        return FusedLocationTrackerImpl(context)
    }

    @Provides @Singleton fun provideOrderRepository(room: RoomOrderRepository): OrderRepository = room
    @Provides @Singleton fun provideTripRepository(room: RoomTripRepository): TripRepository = room
    @Provides @Singleton fun provideTimeBasedExpenseRepository(room: RoomTimeBasedExpenseRepository): TimeBasedExpenseRepository = room
    @Provides @Singleton fun provideDistanceBasedExpenseRepository(room: RoomDistanceBasedExpenseRepository): DistanceBasedExpenseRepository = room

    // Use Cases
    @Provides @Singleton fun provideSyncTimeBasedExpensesUseCase(repository: TimeBasedExpenseRepository) = SyncTimeBasedExpensesUseCase(repository)
    @Provides @Singleton fun provideGetTotalTimeBasedExpensesImpactUseCase(repository: TimeBasedExpenseRepository) = GetTotalTimeBasedExpensesImpactUseCase(repository)
    @Provides @Singleton fun provideGetOrderByIdUseCase(orderRepository: OrderRepository) = GetOrderByIdUseCase(orderRepository)
    @Provides @Singleton fun provideGetOrdersAmountAfterKmUseCase(tripRepository: TripRepository) = GetOrdersAmountAfterKmUseCase(tripRepository)
    @Provides @Singleton fun provideGetAmountNetUseCase(
        getOrdersAmountAfterKmUseCase: GetOrdersAmountAfterKmUseCase,
        getTotalTimeBasedExpensesImpactUseCase: GetTotalTimeBasedExpensesImpactUseCase
    ) = GetAmountNetUseCase(getOrdersAmountAfterKmUseCase, getTotalTimeBasedExpensesImpactUseCase)
    @Provides @Singleton fun provideAddOrderUseCase(orderRepository: OrderRepository) = AddOrderUseCase(orderRepository)
    @Provides @Singleton fun provideGetOrdersFlowUseCase(orderRepository: OrderRepository) = GetOrdersFlowUseCase(orderRepository)
    @Provides @Singleton fun provideGetTimeBasedExpensesUseCase(repository: TimeBasedExpenseRepository) = GetTimeBasedExpensesUseCase(repository)
    @Provides @Singleton fun provideGetTimeBasedExpenseByIdUseCase(repository: TimeBasedExpenseRepository) = GetTimeBasedExpenseByIdUseCase(repository)
    @Provides @Singleton fun provideAddTimeBasedExpenseUseCase(repository: TimeBasedExpenseRepository) = AddTimeBasedExpenseUseCase(repository)
    @Provides @Singleton fun provideD1(r: TimeBasedExpenseRepository) = DeleteTimeBasedExpenseUseCase(r)
    @Provides @Singleton fun provideU1(r: TimeBasedExpenseRepository) = UpdateTimeBasedExpenseUseCase(r)
    @Provides @Singleton fun provideD2(r1: OrderRepository, r2: TripRepository, u1: ProcessTripIncomeUseCase, u2: ReverseTripIncomeUseCase) = DeleteOrderUseCase(r1, r2, u1, u2)
    @Provides @Singleton fun provideU2(r1: OrderRepository, r2: TripRepository, u1: ProcessTripIncomeUseCase, u2: ReverseTripIncomeUseCase) = UpdateOrderUseCase(r1, r2, u1, u2)
    @Provides @Singleton fun provideG8(r: DistanceBasedExpenseRepository) = GetDistanceBasedExpensesUseCase(r)
    @Provides @Singleton fun provideA3(r: DistanceBasedExpenseRepository) = AddDistanceBasedExpenseUseCase(r)
    @Provides @Singleton fun provideD3(r: DistanceBasedExpenseRepository) = DeleteDistanceBasedExpenseUseCase(r)
    @Provides @Singleton fun provideU3(r: DistanceBasedExpenseRepository) = UpdateDistanceBasedExpenseUseCase(r)
    @Provides @Singleton fun provideR1(r: DistanceBasedExpenseRepository) = ResetDistanceExpenseUseCase(r)
    @Provides @Singleton fun provideA4(r: DistanceBasedExpenseRepository) = ApplyKmDeductionUseCase(r)
    @Provides @Singleton fun provideA5(r: TimeBasedExpenseRepository) = ApplyTimeBasedDeductionUseCase(r)
    @Provides @Singleton fun provideP1(u1: ApplyKmDeductionUseCase, u2: ApplyTimeBasedDeductionUseCase) = ProcessTripIncomeUseCase(u1, u2)
    @Provides @Singleton fun provideC1(a: com.control_delivery.finanzas_delivery.domain.trip.ActiveTripManager, u: ProcessTripIncomeUseCase, r1: TripRepository, r2: OrderRepository) = CompleteTripUseCase(a, u, r1, r2)
    @Provides @Singleton fun provideG9(r: TripRepository) = GetTripsFlowUseCase(r)
    @Provides @Singleton fun provideG10(r: TripRepository) = GetTripByIdUseCase(r)
    @Provides @Singleton fun provideR2(r1: TimeBasedExpenseRepository, r2: DistanceBasedExpenseRepository) = ReverseTripIncomeUseCase(r1, r2)
    @Provides @Singleton fun provideD4(r: TripRepository, u: ReverseTripIncomeUseCase) = DeleteTripUseCase(r, u)

    @Provides
    @Singleton
    fun provideOsrmApiService(): com.control_delivery.finanzas_delivery.data.routing.OsrmApiService {
        val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        
        // OSRM public server REQUIRES a descriptive User-Agent
        val userAgentInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "FinanzasDeliveryApp/1.0 (Android; imerc)")
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
    fun provideRouteRepository(apiService: com.control_delivery.finanzas_delivery.data.routing.OsrmApiService): com.control_delivery.finanzas_delivery.domain.routing.RouteRepository = 
        com.control_delivery.finanzas_delivery.data.routing.OsrmRouteRepository(apiService)

    @Provides
    @Singleton
    fun provideGetSnappedRouteUseCase(routeRepository: com.control_delivery.finanzas_delivery.domain.routing.RouteRepository) = 
        com.control_delivery.finanzas_delivery.domain.usecases.GetSnappedRouteUseCase(routeRepository)
}
