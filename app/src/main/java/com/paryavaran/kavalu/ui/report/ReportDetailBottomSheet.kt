package com.paryavaran.kavalu.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.paryavaran.kavalu.data.AppViewModel
import com.paryavaran.kavalu.databinding.BottomSheetReportDetailBinding

class ReportDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetReportDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reportId = arguments?.getString("report_id") ?: return
        val report = viewModel.reports.value?.find { it.id == reportId } ?: return

        binding.tvWasteType.text = report.wasteType
        binding.tvCoords.text = "${String.format("%.6f", report.lat)}°N, ${String.format("%.6f", report.lng)}°E"
        binding.tvSeverity.text = report.severity
        binding.tvReportedTime.text = report.timeAgo

        val isPending = report.status == "pending"
        binding.tvStatus.text = if (isPending) "🔴 Pending Cleanup" else "🟢 Cleaned"

        if (report.description.isNotEmpty()) {
            binding.tvDescription.text = report.description
            binding.layoutDescription.visibility = View.VISIBLE
        }

        if (!report.photoPath.isNullOrEmpty()) {
            binding.ivPhoto.visibility = View.VISIBLE
            Glide.with(this).load(report.photoPath).into(binding.ivPhoto)
        }

        if (isPending) {
            binding.btnMarkCleaned.visibility = View.VISIBLE
            binding.tvAlreadyCleaned.visibility = View.GONE
            binding.btnMarkCleaned.setOnClickListener {
                viewModel.markCleaned(reportId)
                dismiss()
            }
        } else {
            binding.btnMarkCleaned.visibility = View.GONE
            binding.tvAlreadyCleaned.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(reportId: String) = ReportDetailBottomSheet().apply {
            arguments = Bundle().apply { putString("report_id", reportId) }
        }
    }
}
