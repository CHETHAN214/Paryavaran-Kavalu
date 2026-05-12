package com.paryavaran.kavalu

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.paryavaran.kavalu.data.AppViewModel
import com.paryavaran.kavalu.databinding.ActivityMainBinding
import com.paryavaran.kavalu.ui.dashboard.DashboardFragment
import com.paryavaran.kavalu.ui.leaderboard.LeaderboardFragment
import com.paryavaran.kavalu.ui.map.MapFragment
import com.paryavaran.kavalu.ui.report.ReportFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val viewModel: AppViewModel by viewModels()

    private val dashboardFragment = DashboardFragment()
    private val reportFragment = ReportFragment()
    private val mapFragment = MapFragment()
    private val leaderboardFragment = LeaderboardFragment()

    private var currentTab = "dashboard"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFragments()
        setupNavTabs()

        // Update karma in topbar
        viewModel.karma.observe(this) { karma ->
            binding.tvKarmaValue.text = karma.toString()
        }

        binding.layoutKarmaBadge.setOnClickListener { showTab("leaderboard") }
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, dashboardFragment, "dashboard")
            .add(R.id.fragment_container, reportFragment, "report")
            .add(R.id.fragment_container, mapFragment, "map")
            .add(R.id.fragment_container, leaderboardFragment, "leaderboard")
            .hide(reportFragment)
            .hide(mapFragment)
            .hide(leaderboardFragment)
            .commit()
    }

    private fun setupNavTabs() {
        binding.btnTabHome.setOnClickListener { showTab("dashboard") }
        binding.btnTabReport.setOnClickListener { showTab("report") }
        binding.btnTabMap.setOnClickListener { showTab("map") }
        binding.btnTabKarma.setOnClickListener { showTab("leaderboard") }
    }

    fun showTab(tab: String) {
        currentTab = tab
        val transaction = supportFragmentManager.beginTransaction()

        listOf(dashboardFragment, reportFragment, mapFragment, leaderboardFragment).forEach {
            transaction.hide(it)
        }

        when (tab) {
            "dashboard" -> transaction.show(dashboardFragment)
            "report" -> transaction.show(reportFragment)
            "map" -> transaction.show(mapFragment)
            "leaderboard" -> transaction.show(leaderboardFragment)
        }
        transaction.commit()

        // Update tab indicator
        updateTabUI(tab)
    }

    private fun updateTabUI(activeTab: String) {
        listOf(
            "dashboard" to binding.btnTabHome,
            "report" to binding.btnTabReport,
            "map" to binding.btnTabMap,
            "leaderboard" to binding.btnTabKarma
        ).forEach { (tab, btn) ->
            btn.isSelected = tab == activeTab
        }
    }

    fun showToast(message: String, isError: Boolean = false) {
        binding.tvToast.text = message
        binding.tvToast.setBackgroundResource(
            if (isError) R.drawable.bg_toast_error else R.drawable.bg_toast
        )
        binding.tvToast.visibility = View.VISIBLE
        binding.tvToast.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .withStartAction {
                binding.tvToast.alpha = 0f
                binding.tvToast.translationY = 20f
            }
            .start()

        binding.tvToast.postDelayed({
            binding.tvToast.animate()
                .alpha(0f)
                .translationY(20f)
                .setDuration(300)
                .withEndAction { binding.tvToast.visibility = View.GONE }
                .start()
        }, 3000)
    }
}
