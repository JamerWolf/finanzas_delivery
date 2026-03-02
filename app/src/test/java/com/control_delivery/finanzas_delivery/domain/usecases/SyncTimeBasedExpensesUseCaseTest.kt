package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class SyncTimeBasedExpensesUseCaseTest {
    private val repository: TimeBasedExpenseRepository = mockk()
    private val useCase = SyncTimeBasedExpensesUseCase(repository)

    @Test
    fun `invoke should call repository syncExpenses method`(): Unit = runTest {
        val testDate = LocalDate.of(2026,2,26)
        io.mockk.coEvery { repository.syncExpenses(testDate) } just runs

        useCase(testDate)

        io.mockk.coVerify (exactly = 1) { repository.syncExpenses(testDate) }
    }

}