package com.auralis.player.data.scanner

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Punto unico para solicitar un escaneo.
 * Trabajo unico con ExistingWorkPolicy.KEEP: si ya hay un escaneo
 * pendiente o en curso, no se encola otro.
 *
 * Restricciones: sin red requerida y bateria no baja. No se exige
 * cargando/idle para que el primer escaneo arranque pronto.
 *
 * Frontera con reproduccion (fase de audio): el trabajo usa un nombre
 * unico estable; PlaybackService podra cancelarlo por nombre cuando
 * exista reproduccion activa (cuotas de jobs en API 36 durante FGS).
 */
@Singleton
open class LibraryScanScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    open fun requestScan() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = OneTimeWorkRequestBuilder<LibraryScanWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_SECONDS, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            LibraryScanWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    companion object {
        private const val BACKOFF_DELAY_SECONDS = 30L
    }
}
