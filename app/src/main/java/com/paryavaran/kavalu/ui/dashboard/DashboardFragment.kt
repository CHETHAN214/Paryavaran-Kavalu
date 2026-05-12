package com.paryavaran.kavalu.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.paryavaran.kavalu.MainActivity
import com.paryavaran.kavalu.R
import com.paryavaran.kavalu.data.AppLevels
import com.paryavaran.kavalu.data.AppViewModel
import com.paryavaran.kavalu.databinding.FragmentDashboardBinding
import com.paryavaran.kavalu.ui.report.ReportDetailBottomSheet

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppViewModel by activityViewModels()
    private lateinit var recentAdapter: RecentReportsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recentAdapter = RecentReportsAdapter(
            onItemClick = { report ->
                ReportDetailBottomSheet.newInstance(report.id)
                    .show(parentFragmentManager, "detail")
            },
            onMarkCleaned = { report ->
                viewModel.markCleaned(report.id)
                (activity as? MainActivity)?.showToast("🟢 Marked as Cleaned! +5 Eco-Karma")
            }
        )
        binding.rvRecentReports.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentAdapter
        }

        binding.btnReportNew.setOnClickListener {
            (activity as? MainActivity)?.showTab("report")
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.karma.observe(viewLifecycleOwner) { karma ->
            binding.tvKarmaNum.text = karma.toString()
            val level = AppLevels.getLevel(karma)
            binding.tvLevelName.text = "Level ${AppLevels.levels.indexOf(level) + 1} — ${level.name}"
        }

        viewModel.streak.observe(viewLifecycleOwner) { streak ->
            binding.tvStreak.text = "🔥 $streak day streak"
        }

        viewModel.reports.observe(viewLifecycleOwner) { reports ->
            val pending = reports.count { it.status == "pending" }
            val cleaned = reports.count { it.status == "cleaned" }
            val total = reports.size
            val rate = if (total > 0) (cleaned * 100 / total) else 0

            binding.tvChipReports.text = "📍 $total reports"
            binding.tvChipCleaned.text = "✅ $cleaned cleaned"

            binding.tvStatPending.text = pending.toString()
            binding.tvStatCleaned.text = cleaned.toString()
            binding.tvStatRate.text = "$rate%"

            val recent = reports.reversed().take(5)
            if (recent.isEmpty()) {
                binding.rvRecentReports.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvRecentReports.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
                recentAdapter.submitList(recent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
