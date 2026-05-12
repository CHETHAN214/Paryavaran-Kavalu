package com.paryavaran.kavalu.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.paryavaran.kavalu.R
import com.paryavaran.kavalu.data.WasteReport
import com.paryavaran.kavalu.databinding.ItemReportCardBinding

class RecentReportsAdapter(
    private val onItemClick: (WasteReport) -> Unit,
    private val onMarkCleaned: (WasteReport) -> Unit
) : ListAdapter<WasteReport, RecentReportsAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemReportCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemReportCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val report = getItem(position)
        with(holder.binding) {
            tvWasteType.text = report.wasteType
            tvLocation.text = "📍 ${String.format("%.4f", report.lat)}°N, ${String.format("%.4f", report.lng)}°E"
            tvTimeAgo.text = report.timeAgo

            val isPending = report.status == "pending"
            viewStatusDot.setBackgroundResource(
                if (isPending) R.drawable.bg_dot_pending else R.drawable.bg_dot_cleaned
            )
            tvStatus.text = if (isPending) "🔴 Pending" else "🟢 Cleaned"
            tvStatus.setBackgroundResource(
                if (isPending) R.drawable.bg_status_pending else R.drawable.bg_status_cleaned
            )
            tvStatus.setTextColor(
                root.context.getColor(
                    if (isPending) R.color.red else R.color.green_main
                )
            )

            btnMarkCleaned.visibility = if (isPending) View.VISIBLE else View.GONE
            btnMarkCleaned.setOnClickListener { onMarkCleaned(report) }
            root.setOnClickListener { onItemClick(report) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<WasteReport>() {
            override fun areItemsTheSame(a: WasteReport, b: WasteReport) = a.id == b.id
            override fun areContentsTheSame(a: WasteReport, b: WasteReport) = a == b
        }
    }
}
