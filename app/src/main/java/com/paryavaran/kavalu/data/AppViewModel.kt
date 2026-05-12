package com.paryavaran.kavalu.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AppRepository(application)

    val reports = MutableLiveData<MutableList<WasteReport>>(repository.getReports())
    val karma = MutableLiveData(repository.karma)
    val streak = MutableLiveData(repository.streak)

    fun addReport(report: WasteReport) {
        val list = repository.addReport(report).toMutableList()
        reports.value = list
        val karmaEarned = when (report.severity) {
            "High" -> 20
            "Medium" -> 15
            else -> 10
        }
        val newKarma = (karma.value ?: 0) + karmaEarned
        karma.value = newKarma
        repository.karma = newKarma
    }

    fun markCleaned(id: String) {
        repository.updateReportStatus(id, "cleaned")
        reports.value = repository.getReports()
        val newKarma = (karma.value ?: 0) + 5
        karma.value = newKarma
        repository.karma = newKarma
    }

    fun refreshReports() {
        reports.value = repository.getReports()
    }

    fun updateTimeAgo() {
        val now = System.currentTimeMillis()
        val list = repository.getReports()
        list.forEach { r ->
            val mins = ((now - r.timestamp) / 60000).toInt()
            r.timeAgo = when {
                mins < 1 -> "Just now"
                mins < 60 -> "${mins}m ago"
                mins < 1440 -> "${mins / 60}h ago"
                else -> "${mins / 1440}d ago"
            }
        }
        reports.value = list
    }
}
