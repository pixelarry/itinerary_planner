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

import java.util.Date
import java.util.Calendar

data class PlanUiModel(
    val id: Long = 0,
    val title: String,
    val startDate: String,
    val endDate: String,
    val imageUrl: String
)

data class Task(
    val id: Long = 0,
    val planId: Long,
    val title: String,
    val startTime: Date,
    val endTime: Date,
    val duration: Long, // in minutes
    val cost: Double,
    val date: String, // formatted date string
    val isFixedStartTime: Boolean = false
)

data class ItineraryItem(
    val task: Task,
    val displayTime: String,
    val displayDuration: String,
    val displayCost: String
)

object SamplePlansProvider {
    fun samplePlans(): List<PlanUiModel> = listOf(
        PlanUiModel(
            id = 1,
            title = "Singapore Vibes",
            startDate = "9/24/2024",
            endDate = "9/30/2024",
            imageUrl = "https://images.unsplash.com/photo-1541542684-4aef8a4b25c5?w=1200"
        ),
        PlanUiModel(
            id = 2,
            title = "London Dreams",
            startDate = "12/19/2024",
            endDate = "12/29/2024",
            imageUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=1200"
        ),
        PlanUiModel(
            id = 3,
            title = "Dubai Nights",
            startDate = "1/12/2025",
            endDate = "1/19/2025",
            imageUrl = "https://images.unsplash.com/photo-1506197603052-3cc9c3a201bd?w=1200"
        )
    )

    fun sampleTasks(): List<Task> = listOf(
        Task(
            id = 1,
            planId = 1,
            title = "Arrival",
            startTime = Calendar.getInstance().apply { 
                set(2024, 8, 24, 8, 0) 
            }.time,
            endTime = Calendar.getInstance().apply { 
                set(2024, 8, 24, 8, 30) 
            }.time,
            duration = 30,
            cost = 0.0,
            date = "2024-09-24"
        ),
        Task(
            id = 2,
            planId = 1,
            title = "Breakfast",
            startTime = Calendar.getInstance().apply { 
                set(2024, 8, 24, 9, 0) 
            }.time,
            endTime = Calendar.getInstance().apply { 
                set(2024, 8, 24, 9, 30) 
            }.time,
            duration = 30,
            cost = 20.0,
            date = "2024-09-24"
        ),
        Task(
            id = 3,
            planId = 1,
            title = "Travel to Hotel",
            startTime = Calendar.getInstance().apply { 
                set(2024, 8, 24, 10, 0) 
            }.time,
            endTime = Calendar.getInstance().apply { 
                set(2024, 8, 24, 11, 0) 
            }.time,
            duration = 60,
            cost = 40.0,
            date = "2024-09-24"
        ),
        Task(
            id = 4,
            planId = 1,
            title = "Hotel Check-in",
            startTime = Calendar.getInstance().apply { 
                set(2024, 8, 24, 11, 0) 
            }.time,
            endTime = Calendar.getInstance().apply { 
                set(2024, 8, 24, 11, 15) 
            }.time,
            duration = 15,
            cost = 0.0,
            date = "2024-09-24"
        )
    )
}

