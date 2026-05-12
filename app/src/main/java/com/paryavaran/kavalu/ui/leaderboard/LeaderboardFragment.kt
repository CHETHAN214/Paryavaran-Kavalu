package com.paryavaran.kavalu.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.paryavaran.kavalu.data.AppLevels
import com.paryavaran.kavalu.data.AppViewModel
import com.paryavaran.kavalu.databinding.FragmentLeaderboardBinding

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppViewModel by activityViewModels()
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var badgesAdapter: BadgesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leaderboardAdapter = LeaderboardAdapter()
        binding.rvLeaderboard.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = leaderboardAdapter
        }

        badgesAdapter = BadgesAdapter()
        binding.rvBadges.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 4)
            adapter = badgesAdapter
        }

        viewModel.karma.observe(viewLifecycleOwner) { karma ->
            binding.tvKarmaValue.text = karma.toString()
            val level = AppLevels.getLevel(karma)
            val levelIdx = AppLevels.levels.indexOf(level) + 1
            binding.tvLevelLabel.text = "Level $levelIdx — ${level.name}"
            val progress = AppLevels.getProgressPercent(karma)
            binding.progressLevel.progress = progress.toInt()
            if (level.max == Int.MAX_VALUE) {
                binding.tvNextLevel.text = "MAX LEVEL"
            } else {
                binding.tvNextLevel.text = "${level.max - karma} pts to next"
            }

            // Update badges
            val reports = viewModel.reports.value ?: emptyList()
            val cleaned = reports.count { it.status == "cleaned" }
            badgesAdapter.submitList(
                listOf(
                    BadgeItem("🌱", "First Report", reports.isNotEmpty()),
                    BadgeItem("🔥", "3-Day Streak", (viewModel.streak.value ?: 0) >= 3),
                    BadgeItem("🏅", "5 Reports", reports.size >= 5),
                    BadgeItem("🌍", "10 Reports", reports.size >= 10),
                    BadgeItem("♻️", "First Cleanup", cleaned >= 1),
                    BadgeItem("⭐", "Top Reporter", karma >= 50),
                    BadgeItem("💎", "100 Karma", karma >= 100),
                    BadgeItem("🏆", "Champion", karma >= 300),
                )
            )

            // Update leaderboard
            val lbData = viewModel.repository.getDefaultLeaderboard()
                .map { if (it.isMe) it.copy(points = karma) else it }
                .sortedByDescending { it.points }
            leaderboardAdapter.submitList(lbData)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
