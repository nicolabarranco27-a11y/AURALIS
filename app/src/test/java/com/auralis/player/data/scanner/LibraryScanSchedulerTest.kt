package com.auralis.player.data.scanner

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LibraryScanSchedulerTest {

    private lateinit var context: Context
    private lateinit var scheduler: LibraryScanScheduler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )
        scheduler = LibraryScanScheduler(context)
    }

    private fun uniqueWorkInfos(): List<WorkInfo> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(LibraryScanWorker.UNIQUE_WORK_NAME)
            .get()

    @Test
    fun `solicita un trabajo unico con el nombre establecido`() {
        scheduler.requestScan()

        val infos = uniqueWorkInfos()
        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.first().state)
    }

    @Test
    fun `solicitudes repetidas no duplican el escaneo`() {
        scheduler.requestScan()
        scheduler.requestScan()
        scheduler.requestScan()

        assertEquals(1, uniqueWorkInfos().size)
    }

    @Test
    fun `constraints sin red y bateria no baja`() {
        scheduler.requestScan()

        val constraints = uniqueWorkInfos().first().constraints
        assertEquals(NetworkType.NOT_REQUIRED, constraints.requiredNetworkType)
        assertTrue(constraints.requiresBatteryNotLow())
    }
}
