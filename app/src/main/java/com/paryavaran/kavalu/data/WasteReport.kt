package com.paryavaran.kavalu.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WasteReport(
    val id: String,
    val wasteType: String,
    val severity: String,
    val lat: Double,
    val lng: Double,
    val photoPath: String?,
    val description: String,
    var status: String = "pending", // "pending" or "cleaned"
    val timestamp: Long = System.currentTimeMillis(),
    var timeAgo: String = "Just now"
) : Parcelable

enum class WasteType(val label: String, val emoji: String) {
    HOUSEHOLD("Household Waste", "🏠"),
    PLASTIC("Plastic Waste", "♻️"),
    DEBRIS("Construction Debris", "🧱"),
    BIOMEDICAL("Bio-Medical Waste", "⚕️"),
    EWASTE("Electronic Waste", "📱"),
    MIXED("Mixed Waste", "🗂️")
}

enum class Severity(val label: String, val karmaPoints: Int) {
    LOW("Low", 10),
    MEDIUM("Medium", 15),
    HIGH("High", 20)
}

data class LeaderboardUser(
    val name: String,
    val area: String,
    var points: Int,
    val avatar: String,
    val avatarColor: Int?,
    val isMe: Boolean = false
)

data class Level(
    val name: String,
    val min: Int,
    val max: Int
)

object AppLevels {
    val levels = listOf(
        Level("Seedling Guardian", 0, 50),
        Level("Green Warrior", 50, 150),
        Level("Eco Champion", 150, 300),
        Level("Nature Protector", 300, 500),
        Level("Environment Defender", 500, 1000),
        Level("Paryavaran Legend", 1000, Int.MAX_VALUE)
    )

    fun getLevel(karma: Int): Level =
        levels.find { karma >= it.min && karma < it.max } ?: levels.last()

    fun getProgressPercent(karma: Int): Float {
        val level = getLevel(karma)
        if (level.max == Int.MAX_VALUE) return 100f
        val range = (level.max - level.min).toFloat()
        val progress = (karma - level.min).toFloat()
        return (progress / range * 100f).coerceIn(0f, 100f)
    }
}
