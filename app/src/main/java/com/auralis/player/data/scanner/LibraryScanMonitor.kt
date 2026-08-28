package com.auralis.player.data.scanner

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

open class LibraryScanMonitor @Inject constructor() {

    sealed interface Status {
        data object Idle : Status
        data object Running : Status
        data class Finished(val outcome: ScanOutcome) : Status
    }

    private val _status = MutableStateFlow<Status>(Status.Idle)
    open val status: StateFlow<Status> = _status.asStateFlow()

    fun onScanStarted() {
        _status.value = Status.Running
    }

    fun onScanFinished(outcome: ScanOutcome) {
        _status.value = Status.Finished(outcome)
    }

    fun onScanAborted() {
        _status.value = Status.Idle
    }
}
