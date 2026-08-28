package com.auralis.player.data.scanner

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LibraryScanWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val scanner: LibraryScanner,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = when (val outcome = scanner.sync()) {
        is ScanOutcome.Success -> Result.success(
            workDataOf(
                KEY_SCANNED to outcome.scanned,
                KEY_ADDED to outcome.added,
                KEY_UPDATED to outcome.updated,
                KEY_REMOVED to outcome.removed,
            ),
        )

        is ScanOutcome.PermissionDenied -> Result.failure(
            workDataOf(KEY_ERROR to ERROR_PERMISSION_DENIED),
        )

        is ScanOutcome.Failed -> {
            if (runAttemptCount < MAX_ATTEMPTS - 1) {
                Result.retry()
            } else {
                Result.failure(workDataOf(KEY_ERROR to outcome.message))
            }
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "auralis_library_scan"
        const val KEY_ERROR = "error"
        const val KEY_SCANNED = "scanned"
        const val KEY_ADDED = "added"
        const val KEY_UPDATED = "updated"
        const val KEY_REMOVED = "removed"
        const val ERROR_PERMISSION_DENIED = "permission_denied"

        /** Intentos totales permitidos antes de FAILURE definitivo. */
        const val MAX_ATTEMPTS = 3
    }
}
