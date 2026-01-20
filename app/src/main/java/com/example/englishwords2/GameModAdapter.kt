package com.example.englishwords2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.englishwords2.databinding.ItemGameModeBinding

class GameModeAdapter(
    private val list: List<GameMode>,
    private val onClick: (GameMode) -> Unit
) : RecyclerView.Adapter<GameModeAdapter.GameModeVH>() {

    inner class GameModeVH(val binding: ItemGameModeBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameModeVH {
        val binding = ItemGameModeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GameModeVH(binding)
    }

    override fun onBindViewHolder(holder: GameModeVH, position: Int) {
        val mode = list[position]

        holder.binding.txtTitle.text = mode.title
        holder.binding.txtDesc.text = mode.subtitle
        holder.binding.txtIcon.text = when (mode.tur) {
            "GENEL" -> "📘"
            "VERBS" -> "⚡"
            "ADJECTIVES" -> "🎯"
            else -> "🎮"
        }
        holder.binding.lockLayout.visibility =
            if (mode.isLocked) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onClick(mode)
        }
    }

    override fun getItemCount() = list.size
}
