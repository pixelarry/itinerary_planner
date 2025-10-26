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

package com.pixelarry.itinerary_planner.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pixelarry.itinerary_planner.ui.PlanUiModel
import com.pixelarry.itinerary_planner.ui.Task
import java.text.SimpleDateFormat
import java.util.*

class PlansDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_PLANS (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_TITLE TEXT NOT NULL, " +
                "$COL_START TEXT NOT NULL, " +
                "$COL_END TEXT NOT NULL, " +
                "$COL_IMAGE TEXT NOT NULL DEFAULT ''" +
            ")"
        )
        
        db.execSQL(
            "CREATE TABLE $TABLE_TASKS (" +
                "$COL_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_PLAN_ID INTEGER NOT NULL, " +
                "$COL_TITLE_TASK TEXT NOT NULL, " +
                "$COL_START_TIME TEXT NOT NULL, " +
                "$COL_END_TIME TEXT NOT NULL, " +
                "$COL_DURATION INTEGER NOT NULL, " +
                "$COL_COST REAL NOT NULL, " +
                "$COL_DATE TEXT NOT NULL, " +
                "FOREIGN KEY($COL_PLAN_ID) REFERENCES $TABLE_PLANS($COL_ID) ON DELETE CASCADE" +
            ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLANS")
        onCreate(db)
    }

    fun insertPlan(plan: PlanUiModel) {
        val values = ContentValues().apply {
            put(COL_TITLE, plan.title)
            put(COL_START, plan.startDate)
            put(COL_END, plan.endDate)
            put(COL_IMAGE, plan.imageUrl)
        }
        writableDatabase.insert(TABLE_PLANS, null, values)
    }

    fun getAllPlans(): List<PlanUiModel> {
        val list = mutableListOf<PlanUiModel>()
        val cursor = readableDatabase.query(
            TABLE_PLANS,
            arrayOf(COL_ID, COL_TITLE, COL_START, COL_END, COL_IMAGE),
            null, null, null, null, "$COL_ID DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    PlanUiModel(
                        id = it.getLong(0),
                        title = it.getString(1),
                        startDate = it.getString(2),
                        endDate = it.getString(3),
                        imageUrl = it.getString(4)
                    )
                )
            }
        }
        return list
    }

    fun insertTask(task: Task): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val values = ContentValues().apply {
            put(COL_PLAN_ID, task.planId)
            put(COL_TITLE_TASK, task.title)
            put(COL_START_TIME, dateFormat.format(task.startTime))
            put(COL_END_TIME, dateFormat.format(task.endTime))
            put(COL_DURATION, task.duration)
            put(COL_COST, task.cost)
            put(COL_DATE, task.date)
        }
        return writableDatabase.insert(TABLE_TASKS, null, values)
    }

    fun getTasksForPlan(planId: Long): List<Task> {
        val list = mutableListOf<Task>()
        val cursor = readableDatabase.query(
            TABLE_TASKS,
            arrayOf(COL_TASK_ID, COL_PLAN_ID, COL_TITLE_TASK, COL_START_TIME, COL_END_TIME, COL_DURATION, COL_COST, COL_DATE),
            "$COL_PLAN_ID = ?",
            arrayOf(planId.toString()),
            null, null, "$COL_DATE ASC, $COL_START_TIME ASC"
        )
        cursor.use {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            while (it.moveToNext()) {
                list.add(
                    Task(
                        id = it.getLong(0),
                        planId = it.getLong(1),
                        title = it.getString(2),
                        startTime = dateFormat.parse(it.getString(3)) ?: Date(),
                        endTime = dateFormat.parse(it.getString(4)) ?: Date(),
                        duration = it.getLong(5),
                        cost = it.getDouble(6),
                        date = it.getString(7)
                    )
                )
            }
        }
        return list
    }

    fun deleteTask(taskId: Long): Int {
        return writableDatabase.delete(TABLE_TASKS, "$COL_TASK_ID = ?", arrayOf(taskId.toString()))
    }

    fun updateTaskOrder(taskId: Long, newOrder: Int) {
        val values = ContentValues().apply {
            put(COL_TASK_ID, newOrder)
        }
        writableDatabase.update(TABLE_TASKS, values, "$COL_TASK_ID = ?", arrayOf(taskId.toString()))
    }

    fun deletePlan(planId: Long): Int {
        // Delete the plan - tasks will be automatically deleted due to CASCADE constraint
        return writableDatabase.delete(TABLE_PLANS, "$COL_ID = ?", arrayOf(planId.toString()))
    }

    companion object {
        private const val DB_NAME = "plans.db"
        private const val DB_VERSION = 3

        private const val TABLE_PLANS = "plans"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_START = "start"
        private const val COL_END = "end"
        private const val COL_IMAGE = "image"

        private const val TABLE_TASKS = "tasks"
        private const val COL_TASK_ID = "id"
        private const val COL_PLAN_ID = "plan_id"
        private const val COL_TITLE_TASK = "title"
        private const val COL_START_TIME = "start_time"
        private const val COL_END_TIME = "end_time"
        private const val COL_DURATION = "duration"
        private const val COL_COST = "cost"
        private const val COL_DATE = "date"
    }
}

