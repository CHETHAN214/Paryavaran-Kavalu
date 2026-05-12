package com.paryavaran.kavalu.ui.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.paryavaran.kavalu.R
import com.paryavaran.kavalu.databinding.ItemBadgeBinding

data class BadgeItem(val emoji: String, val name: String, val earned: Boolean)

class BadgesAdapter : ListAdapter<BadgeItem, BadgesAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemBadgeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val badge = getItem(position)
        with(holder.binding) {
            tvBadgeIcon.text = badge.emoji
            tvBadgeName.text = badge.name
            tvBadgeIcon.alpha = if (badge.earned) 1f else 0.35f
            tvBadgeIcon.setBackgroundResource(
                if (badge.earned) R.drawable.bg_badge_earned else R.drawable.bg_badge_locked
            )
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<BadgeItem>() {
            override fun areItemsTheSame(a: BadgeItem, b: BadgeItem) = a.name == b.name
            override fun areContentsTheSame(a: BadgeItem, b: BadgeItem) = a == b
        }
    }
}
