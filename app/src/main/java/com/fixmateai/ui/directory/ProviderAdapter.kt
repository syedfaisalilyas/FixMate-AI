package com.fixmateai.ui.directory

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fixmateai.R
import com.fixmateai.data.model.ServiceProvider
import com.fixmateai.databinding.ItemProviderBinding
import com.fixmateai.utils.show

/**
 * RecyclerView adapter for the provider directory. Shows each provider's name,
 * trade, rating, city, verified badge and availability.
 */
class ProviderAdapter(
    private val onClick: (ServiceProvider) -> Unit
) : ListAdapter<ServiceProvider, ProviderAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemProviderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(p: ServiceProvider) {
            val ctx = binding.root.context
            binding.tvName.text = p.name
            binding.tvTrade.text = p.trade
            binding.tvRating.text = p.ratingLabel
            binding.tvCity.text = if (p.city.isBlank()) "" else "• ${p.city}"
            binding.ivVerified.show(p.verified)

            val available = p.available
            binding.tvAvailability.text =
                ctx.getString(if (available) R.string.available_now else R.string.unavailable)
            val bg = if (available) R.color.success_light else R.color.surface_muted
            val fg = if (available) R.color.success else R.color.text_secondary
            binding.tvAvailability.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, bg))
            binding.tvAvailability.setTextColor(ContextCompat.getColor(ctx, fg))

            binding.root.setOnClickListener { onClick(p) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProviderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ServiceProvider>() {
            override fun areItemsTheSame(old: ServiceProvider, new: ServiceProvider) =
                old.uid == new.uid

            override fun areContentsTheSame(old: ServiceProvider, new: ServiceProvider) =
                old == new
        }
    }
}
