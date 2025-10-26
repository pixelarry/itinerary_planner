/*
 * Copyright (C) 2025 Pixelarry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.pixelarry.itinerary_planner.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pixelarry.itinerary_planner.PlanDetailsActivity
import com.pixelarry.itinerary_planner.R

class PlansAdapter(
    private val plans: List<PlanUiModel>,
    private val onPlanClicked: (PlanUiModel) -> Unit,
    private val onPlanLongClicked: (PlanUiModel) -> Unit
) : RecyclerView.Adapter<PlansAdapter.PlanViewHolder>() {

    class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.planImage)
        val titleView: TextView = itemView.findViewById(R.id.planTitle)
        val datesView: TextView = itemView.findViewById(R.id.planDates)
        val cardView: View = itemView.findViewById(R.id.planCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plan_card, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = plans[position]
        holder.titleView.text = plan.title
        
        // Convert dates to YYYY/MM/DD format for display
        val startDate = convertToDisplayFormat(plan.startDate)
        val endDate = convertToDisplayFormat(plan.endDate)
        holder.datesView.text = "$startDate - $endDate"
        
        if (plan.imageUrl.isNotBlank()) {
            Glide.with(holder.imageView.context)
                .load(plan.imageUrl)
                .centerCrop()
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(com.pixelarry.itinerary_planner.R.drawable.ic_launcher_background)
        }

        // Set click listener
        holder.cardView.setOnClickListener {
            onPlanClicked(plan)
        }

        // Set long click listener for delete functionality
        holder.cardView.setOnLongClickListener {
            onPlanLongClicked(plan)
            true // Return true to indicate the event was handled
        }
    }
    
    private fun convertToDisplayFormat(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString // Return original if parsing fails
        }
    }

    override fun getItemCount(): Int = plans.size
}

