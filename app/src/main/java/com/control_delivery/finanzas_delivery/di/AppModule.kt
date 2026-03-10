package com.control_delivery.finanzas_delivery.di

import com.control_delivery.finanzas_delivery.data.repository.DistanceBasedExpenseInMemoryRepository
import com.control_delivery.finanzas_delivery.data.repository.OrderInMemoryRepository
import com.control_delivery.finanzas_delivery.data.repository.TimeBasedExpenseInMemoryRepository
import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository
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
        orderRepository: OrderRepository
    ): GetOrdersAmountAfterKmUseCase {
        return GetOrdersAmountAfterKmUseCase(orderRepository)
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
        timeBasedExpenseRepository: TimeBasedExpenseRepository
    ): DeleteOrderUseCase {
        return DeleteOrderUseCase(orderRepository, timeBasedExpenseRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateOrderUseCase(
        orderRepository: OrderRepository,
        timeBasedExpenseRepository: TimeBasedExpenseRepository,
        processOrderIncomeUseCase: ProcessOrderIncomeUseCase
    ): UpdateOrderUseCase {
        return UpdateOrderUseCase(orderRepository, timeBasedExpenseRepository, processOrderIncomeUseCase)
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
    fun provideProcessOrderIncomeUseCase(
        kmFilter: ApplyKmDeductionUseCase,
        timeFilter: ApplyTimeBasedDeductionUseCase
    ): ProcessOrderIncomeUseCase = ProcessOrderIncomeUseCase(kmFilter, timeFilter)
}
