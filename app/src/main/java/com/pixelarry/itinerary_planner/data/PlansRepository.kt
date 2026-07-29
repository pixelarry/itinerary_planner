/*
 * Copyright (C) 2025-2026 sanandmv7
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

package com.pixelarry.itinerary_planner.data

import android.content.Context
import com.pixelarry.itinerary_planner.ui.PlanUiModel

class PlansRepository(context: Context) {
    private val db = PlansDatabase(context.applicationContext)

    fun getPlans(): List<PlanUiModel> = db.getAllPlans()

    fun addPlan(plan: PlanUiModel) = db.insertPlan(plan)

    fun deletePlan(planId: Long): Int = db.deletePlan(planId)
}

