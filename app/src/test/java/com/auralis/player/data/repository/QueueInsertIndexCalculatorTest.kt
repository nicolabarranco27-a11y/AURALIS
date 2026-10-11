package com.auralis.player.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class QueueInsertIndexCalculatorTest {

    @Test
    fun `cola vacia inserta en 0 sin desbordar`() {
        val index = QueueInsertIndexCalculator.calculate(
            currentIndex = -1,
            lastQueuedIndex = 0,
            itemCount = 0,
        )

        assertEquals(0, index)
    }

    @Test
    fun `ultimo indice desfasado se acota al tamano de la cola`() {
        val index = QueueInsertIndexCalculator.calculate(
            currentIndex = -1,
            lastQueuedIndex = 5,
            itemCount = 2,
        )

        assertEquals(2, index)
    }

    @Test
    fun `inserta despues del actual cuando el ultimo quedo atras`() {
        val index = QueueInsertIndexCalculator.calculate(
            currentIndex = 3,
            lastQueuedIndex = 1,
            itemCount = 5,
        )

        assertEquals(4, index)
    }

    @Test
    fun `inserta despues del ultimo agregado manteniendo el orden`() {
        val index = QueueInsertIndexCalculator.calculate(
            currentIndex = 0,
            lastQueuedIndex = 2,
            itemCount = 5,
        )

        assertEquals(3, index)
    }
}
