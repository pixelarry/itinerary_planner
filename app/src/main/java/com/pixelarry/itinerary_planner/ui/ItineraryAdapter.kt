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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pixelarry.itinerary_planner.R
import java.text.SimpleDateFormat
import java.util.*

class ItineraryAdapter(
    private var tasks: List<Task>,
    private val onTaskRemoved: (Task) -> Unit,
    private val onTaskEdit: (Task) -> Unit = {},
    private val onTaskDelay: (Task, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<ItineraryAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    var isEditMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks.sortedWith(compareBy({ it.date }, { it.startTime }))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_itinerary_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, position)
    }

    override fun getItemCount(): Int = tasks.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateHeader: TextView = itemView.findViewById(R.id.dateHeader)
        private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        private val taskTime: TextView = itemView.findViewById(R.id.taskTime)
        private val taskDurationAndCost: TextView = itemView.findViewById(R.id.taskDurationAndCost)
        private val removeTaskButton: ImageButton = itemView.findViewById(R.id.removeTaskButton)
        private val fixedTimeIndicator: TextView = itemView.findViewById(R.id.fixedTimeIndicator)
        private val delayButtonsContainer: View = itemView.findViewById(R.id.delayButtonsContainer)
        private val delayFiveButton: Button = itemView.findViewById(R.id.delayFiveButton)
        private val delayTenButton: Button = itemView.findViewById(R.id.delayTenButton)

        fun bind(task: Task, position: Int) {
            // Show date header if it's the first task of the day or if it's different from previous
            val showDateHeader = position == 0 || 
                (position > 0 && tasks[position - 1].date != task.date)
            
            dateHeader.visibility = if (showDateHeader) View.VISIBLE else View.GONE
            if (showDateHeader) {
                dateHeader.text = formatDate(task.date)
            }

            taskTitle.text = task.title
            taskTime.text = "${timeFormat.format(task.startTime)} - ${timeFormat.format(task.endTime)}"
            
            val durationHours = task.duration / 60
            val durationMinutes = task.duration % 60
            val durationText = when {
                durationHours > 0 && durationMinutes > 0 -> "${durationHours}h ${durationMinutes}m"
                durationHours > 0 -> "${durationHours}h 0m"
                else -> "0h ${durationMinutes}m"
            }
            
            taskDurationAndCost.text = "$durationText • $${String.format("%.2f", task.cost)}"

            // Fixed time indicator
            fixedTimeIndicator.visibility = if (task.isFixedStartTime) View.VISIBLE else View.GONE

            // Edit/Delete mode
            if (isEditMode) {
                removeTaskButton.setImageResource(R.drawable.ic_edit)
                removeTaskButton.imageTintList = android.content.res.ColorStateList.valueOf(
                    itemView.context.getColor(R.color.fab_blue)
                )
                removeTaskButton.contentDescription = "Edit Task"
                removeTaskButton.setOnClickListener {
                    onTaskEdit(task)
                }
            } else {
                removeTaskButton.setImageResource(R.drawable.ic_delete)
                removeTaskButton.imageTintList = android.content.res.ColorStateList.valueOf(
                    itemView.context.getColor(android.R.color.holo_red_dark)
                )
                removeTaskButton.contentDescription = "Remove Task"
                removeTaskButton.setOnClickListener {
                    onTaskRemoved(task)
                }
            }

            // Delay buttons - only visible in edit mode
            delayButtonsContainer.visibility = if (isEditMode) View.VISIBLE else View.GONE
            delayFiveButton.setOnClickListener {
                onTaskDelay(task, 5)
            }
            delayTenButton.setOnClickListener {
                onTaskDelay(task, 10)
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                date?.let { dateFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }
    }
}
