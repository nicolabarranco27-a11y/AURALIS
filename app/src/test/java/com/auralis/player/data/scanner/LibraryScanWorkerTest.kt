package com.auralis.player.data.scanner

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LibraryScanWorkerTest {

    private lateinit var context: Context

    private class FakeScanner(var outcome: ScanOutcome) : LibraryScanner {
        override suspend fun sync(): ScanOutcome = outcome
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    private fun buildWorker(scanner: LibraryScanner): LibraryScanWorker {
        val builder = TestListenableWorkerBuilder<LibraryScanWorker>(context)
        builder.setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters,
            ): ListenableWorker = LibraryScanWorker(appContext, workerParameters, scanner)
        })
        return builder.build()
    }

    @Test
    fun `exito devuelve success con conteos`() = runTest {
        val worker = buildWorker(FakeScanner(ScanOutcome.Success(scanned = 10, added = 3, updated = 2, removed = 1)))

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        val data = (result as ListenableWorker.Result.Success).outputData
        assertEquals(10, data.getInt(LibraryScanWorker.KEY_SCANNED, -1))
        assertEquals(3, data.getInt(LibraryScanWorker.KEY_ADDED, -1))
        assertEquals(2, data.getInt(LibraryScanWorker.KEY_UPDATED, -1))
        assertEquals(1, data.getInt(LibraryScanWorker.KEY_REMOVED, -1))
    }

    @Test
    fun `permiso denegado devuelve failure controlado`() = runTest {
        val worker = buildWorker(FakeScanner(ScanOutcome.PermissionDenied))

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
        val output = (result as ListenableWorker.Result.Failure).outputData
        assertEquals(
            LibraryScanWorker.ERROR_PERMISSION_DENIED,
            output.getString(LibraryScanWorker.KEY_ERROR),
        )
    }

    @Test
    fun `fallo transitorio en primer intento produce retry`() = runTest {
        val worker = buildWorker(FakeScanner(ScanOutcome.Failed("io error")))

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun `fallo tras agotar intentos produce failure con mensaje`() = runTest {
        val builder = TestListenableWorkerBuilder<LibraryScanWorker>(context)
        builder.setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters,
            ): ListenableWorker = LibraryScanWorker(appContext, workerParameters, FakeScanner(ScanOutcome.Failed("io error")))
        })
        builder.setRunAttemptCount(LibraryScanWorker.MAX_ATTEMPTS)
        val worker = builder.build()

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            "io error",
            (result as ListenableWorker.Result.Failure).outputData.getString(LibraryScanWorker.KEY_ERROR),
        )
    }

    @Test
    fun `los datos de salida usan las claves documentadas`() {
        val data = workDataOf(LibraryScanWorker.KEY_ERROR to "x")
        assertEquals("x", data.getString(LibraryScanWorker.KEY_ERROR))
    }
}
