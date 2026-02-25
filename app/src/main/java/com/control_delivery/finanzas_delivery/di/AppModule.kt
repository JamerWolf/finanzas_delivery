package com.control_delivery.finanzas_delivery.di

import com.control_delivery.finanzas_delivery.data.repository.OrderInMemoryRepository
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import com.control_delivery.finanzas_delivery.domain.usecases.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
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
    fun provideGetOrdersTotalAmountUseCase(
        orderRepository: OrderRepository
    ): GetOrdersTotalAmountUseCase {
        return GetOrdersTotalAmountUseCase(orderRepository)
    }

    @Provides
    @Singleton
    fun provideAddOrderUseCase(
        orderRepository: OrderRepository
    ): AddOrderUseCase {
        return AddOrderUseCase(orderRepository)
    }

    @Provides
    @Singleton
    fun provideGetOrdersFlowUseCase(
        orderRepository: OrderRepository
    ): GetOrdersFlowUseCase {
        return GetOrdersFlowUseCase(orderRepository)
    }
}
