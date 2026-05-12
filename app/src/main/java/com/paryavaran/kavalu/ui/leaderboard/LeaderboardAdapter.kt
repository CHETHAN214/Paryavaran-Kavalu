package com.paryavaran.kavalu.ui.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.paryavaran.kavalu.R
import com.paryavaran.kavalu.data.LeaderboardUser
import com.paryavaran.kavalu.databinding.ItemLeaderboardBinding

class LeaderboardAdapter : ListAdapter<LeaderboardUser, LeaderboardAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val user = getItem(position)
        with(holder.binding) {
            tvRankNum.text = (position + 1).toString()
            tvRankNum.setTextColor(
                root.context.getColor(
                    when (position) {
                        0 -> R.color.rank_gold
                        1 -> R.color.rank_silver
                        2 -> R.color.rank_bronze
                        else -> R.color.green_main
                    }
                )
            )
            tvAvatar.text = user.avatar
            if (user.avatarColor != null) {
                tvAvatar.setBackgroundColor(user.avatarColor)
            } else {
                tvAvatar.setBackgroundResource(R.drawable.bg_avatar_me)
            }
            tvName.text = user.name
            tvArea.text = user.area
            tvPoints.text = user.points.toString()

            root.setBackgroundResource(
                if (user.isMe) R.drawable.bg_rank_card_me else R.drawable.bg_rank_card
            )
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<LeaderboardUser>() {
            override fun areItemsTheSame(a: LeaderboardUser, b: LeaderboardUser) = a.name == b.name
            override fun areContentsTheSame(a: LeaderboardUser, b: LeaderboardUser) = a == b
        }
    }
}
