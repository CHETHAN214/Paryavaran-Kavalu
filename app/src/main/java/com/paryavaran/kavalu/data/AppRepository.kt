package com.paryavaran.kavalu.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("paryavaran_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var karma: Int
        get() = prefs.getInt("karma", 0)
        set(value) = prefs.edit().putInt("karma", value).apply()

    var streak: Int
        get() = prefs.getInt("streak", 1)
        set(value) = prefs.edit().putInt("streak", value).apply()

    fun getReports(): MutableList<WasteReport> {
        val json = prefs.getString("reports", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<WasteReport>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun saveReports(reports: List<WasteReport>) {
        prefs.edit().putString("reports", gson.toJson(reports)).apply()
    }

    fun addReport(report: WasteReport): List<WasteReport> {
        val reports = getReports()
        reports.add(report)
        saveReports(reports)
        return reports
    }

    fun updateReportStatus(id: String, status: String): Boolean {
        val reports = getReports()
        val report = reports.find { it.id == id } ?: return false
        val index = reports.indexOf(report)
        reports[index] = report.copy(status = status)
        saveReports(reports)
        return true
    }

    fun getDefaultLeaderboard(): List<LeaderboardUser> = listOf(
        LeaderboardUser("Arjun Sharma", "Koramangala", 420, "🌳", 0xFFD4F5E2.toInt()),
        LeaderboardUser("Priya Patel", "Indiranagar", 385, "🌿", 0xFFC8E6FF.toInt()),
        LeaderboardUser("Rahul Verma", "HSR Layout", 310, "♻️", 0xFFFFF3CD.toInt()),
        LeaderboardUser("You", "Your Area", karma, "😊", null, isMe = true),
        LeaderboardUser("Kavya Nair", "Whitefield", 145, "🌺", 0xFFFCE4EC.toInt()),
        LeaderboardUser("Suresh Kumar", "Banashankari", 98, "🍀", 0xFFE8F5E9.toInt()),
    )
}
