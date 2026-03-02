package com.control_delivery.finanzas_delivery.di

import com.control_delivery.finanzas_delivery.data.repository.OrderInMemoryRepository
import com.control_delivery.finanzas_delivery.data.repository.TimeBasedExpenseInMemoryRepository
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import com.control_delivery.finanzas_delivery.domain.usecases.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
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

    // --- USE CASES (PURES) ---

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
    fun provideGetOrdersTotalAmountUseCase(
        orderRepository: OrderRepository
    ): GetOrdersTotalAmountUseCase {
        return GetOrdersTotalAmountUseCase(orderRepository)
    }

    @Provides
    @Singleton
    fun provideGetAmountNetUseCase(
        getOrdersTotalAmountUseCase: GetOrdersTotalAmountUseCase,
        getTotalTimeBasedExpensesImpactUseCase: GetTotalTimeBasedExpensesImpactUseCase
    ): GetAmountNetUseCase {
        return GetAmountNetUseCase(
            getOrdersTotalAmountUseCase,
            getTotalTimeBasedExpensesImpactUseCase
        )
    }

    @Provides
    @Singleton
    fun provideAddOrderUseCase(
        orderRepository: OrderRepository,
        timeBasedExpenseRepository: TimeBasedExpenseRepository
    ): AddOrderUseCase {
        return AddOrderUseCase(
            orderRepository,
            timeBasedExpenseRepository
        )
    }

    @Provides
    @Singleton
    fun provideGetOrdersFlowUseCase(
        orderRepository: OrderRepository
    ): GetOrdersFlowUseCase {
        return GetOrdersFlowUseCase(orderRepository)
    }
}
