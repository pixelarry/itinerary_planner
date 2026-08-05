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

package com.pixelarry.itinerary_planner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.pixelarry.itinerary_planner.data.PlansDatabase
import com.pixelarry.itinerary_planner.ui.ItineraryAdapter
import com.pixelarry.itinerary_planner.ui.Task
import java.text.SimpleDateFormat
import java.util.*

class PlanDetailsActivity : AppCompatActivity() {
    private lateinit var planTitle: TextView
    private lateinit var planDates: TextView
    private lateinit var planHeaderImage: View
    private lateinit var emptyStateText: TextView
    private lateinit var itineraryRecyclerView: RecyclerView
    private lateinit var addTaskFab: FloatingActionButton
    private lateinit var editModeButton: android.widget.ImageButton
    
    private var planId: Long = 0
    private var planTitleText: String = ""
    private var planStartDate: String = ""
    private var planEndDate: String = ""
    private var planImageUrl: String = ""
    private var isEditMode: Boolean = false
    
    private lateinit var database: PlansDatabase
    private lateinit var adapter: ItineraryAdapter
    private var tasks: MutableList<Task> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_plan_details)

        setupEdgeToEdge()

        // Get plan data from intent
        planId = intent.getLongExtra("plan_id", 0)
        planTitleText = intent.getStringExtra("plan_title") ?: ""
        planStartDate = intent.getStringExtra("plan_start_date") ?: ""
        planEndDate = intent.getStringExtra("plan_end_date") ?: ""
        planImageUrl = intent.getStringExtra("plan_image_url") ?: ""

        if (planId == 0L) {
            finish()
            return
        }

        initializeViews()
        loadPlanData()
        setupRecyclerView()
        loadTasks()
        setupClickListeners()
    }

    private fun setupEdgeToEdge() {
        findViewById<View>(R.id.headerContainer).applySystemBarsPadding(applyTop = true)

        val recyclerView = findViewById<RecyclerView>(R.id.itineraryRecyclerView)
        recyclerView.clipToPadding = false
        recyclerView.applySystemBarsPadding(applyBottom = true)

        findViewById<TextView>(R.id.emptyStateText).applySystemBarsPadding(applyBottom = true)
        findViewById<FloatingActionButton>(R.id.addTaskFab).applySystemBarsMargins(applyRight = true, applyBottom = true)
    }

    private fun initializeViews() {
        planTitle = findViewById(R.id.planTitle)
        planDates = findViewById(R.id.planDates)
        planHeaderImage = findViewById(R.id.planHeaderImage)
        emptyStateText = findViewById(R.id.emptyStateText)
        itineraryRecyclerView = findViewById(R.id.itineraryRecyclerView)
        addTaskFab = findViewById(R.id.addTaskFab)
        editModeButton = findViewById(R.id.editModeButton)
    }

    private fun loadPlanData() {
        planTitle.text = planTitleText
        
        // Format dates for display - remove day of week and show month, day, and year
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val startDate = try {
            SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault()).parse(planStartDate)
        } catch (e: Exception) {
            null
        }
        val endDate = try {
            SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault()).parse(planEndDate)
        } catch (e: Exception) {
            null
        }
        
        val formattedStartDate = startDate?.let { dateFormat.format(it) } ?: planStartDate
        val formattedEndDate = endDate?.let { dateFormat.format(it) } ?: planEndDate
        
        // Calculate and display total cost
        val totalCost = calculateTotalCost()
        val costFormat = String.format(Locale.getDefault(), "%.2f", totalCost)
        val currencySymbol = MainActivity.getCurrencySymbol(this)
        planDates.text = "$formattedStartDate - $formattedEndDate • $currencySymbol$costFormat"

        // Load image using Glide
        if (planImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(planImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(planHeaderImage as android.widget.ImageView)
        }
    }

    private fun setupRecyclerView() {
        adapter = ItineraryAdapter(
            tasks.toList(),
            onTaskRemoved = { task -> removeTask(task) },
            onTaskEdit = { task -> showEditTaskDialog(task) },
            onTaskDelay = { task, minutes -> delayTask(task, minutes) }
        )
        itineraryRecyclerView.layoutManager = LinearLayoutManager(this)
        itineraryRecyclerView.adapter = adapter
    }

    private fun loadTasks() {
        database = PlansDatabase(this)
        tasks.clear()
        tasks.addAll(database.getTasksForPlan(planId))
        updateUI()
    }

    private fun updateUI() {
        if (tasks.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            itineraryRecyclerView.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            itineraryRecyclerView.visibility = View.VISIBLE
            adapter.updateTasks(tasks)
        }
        // Update cost display when tasks change
        updateCostDisplay()
    }

    private fun setupClickListeners() {
        addTaskFab.setOnClickListener {
            showAddTaskDialog()
        }

        findViewById<View>(R.id.closeButton).setOnClickListener {
            finish()
        }

        editModeButton.setOnClickListener {
            toggleEditMode()
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        adapter.isEditMode = isEditMode
        
        if (isEditMode) {
            editModeButton.imageTintList = android.content.res.ColorStateList.valueOf(
                getColor(R.color.fab_blue)
            )
        } else {
            editModeButton.imageTintList = android.content.res.ColorStateList.valueOf(
                getColor(android.R.color.darker_gray)
            )
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.taskTitleInput)
        val dateInput = dialogView.findViewById<TextInputEditText>(R.id.dateInput)
        val startTimeInput = dialogView.findViewById<TextInputEditText>(R.id.startTimeInput)
        val endTimeInput = dialogView.findViewById<TextInputEditText>(R.id.endTimeInput)
        val costInput = dialogView.findViewById<TextInputEditText>(R.id.costInput)
        val fixedStartTimeCheckbox = dialogView.findViewById<android.widget.CheckBox>(R.id.fixedStartTimeCheckbox)

        // Parse trip start and end dates
        val tripStartDate = parseTripDate(planStartDate)
        val tripEndDate = parseTripDate(planEndDate)
        
        // Set default date to trip start date
        val defaultDate = tripStartDate ?: Calendar.getInstance()

        val today = Calendar.getInstance()
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateInput.setText(dateFormat.format(defaultDate.time))

        // Date picker with trip date range restrictions
        dateInput.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    
                    // Validate that selected date is within trip date range
                    if (tripStartDate != null && tripEndDate != null) {
                        if (selectedCalendar.before(tripStartDate) || selectedCalendar.after(tripEndDate)) {
                            Toast.makeText(this, "Date must be within trip dates: ${dateFormat.format(tripStartDate.time)} to ${dateFormat.format(tripEndDate.time)}", Toast.LENGTH_LONG).show()
                            return@DatePickerDialog
                        }
                    }
                    
                    dateInput.setText(dateFormat.format(selectedCalendar.time))
                },
                defaultDate.get(Calendar.YEAR),
                defaultDate.get(Calendar.MONTH),
                defaultDate.get(Calendar.DAY_OF_MONTH)
            )
            
            // Set date picker restrictions based on trip dates
            if (tripStartDate != null && tripEndDate != null) {
                datePickerDialog.datePicker.minDate = tripStartDate.timeInMillis
                datePickerDialog.datePicker.maxDate = tripEndDate.timeInMillis
            }
            
            datePickerDialog.show()
        }

        // Start time picker
        startTimeInput.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    startTimeInput.setText(timeFormat.format(calendar.time))
                    
                    // If end time is not set or is before/equal to start time, set a default end time
                    val endTimeText = endTimeInput.text.toString()
                    if (endTimeText.isEmpty()) {
                        val endCalendar = Calendar.getInstance()
                        endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        endCalendar.set(Calendar.MINUTE, minute + 30) // Default 30 minutes duration
                        if (endCalendar.get(Calendar.MINUTE) >= 60) {
                            endCalendar.add(Calendar.HOUR_OF_DAY, 1)
                            endCalendar.set(Calendar.MINUTE, endCalendar.get(Calendar.MINUTE) - 60)
                        }
                        endTimeInput.setText(timeFormat.format(endCalendar.time))
                    } else {
                        // Check if current end time is valid
                        val endTimeDate = timeFormat.parse(endTimeText)
                        if (endTimeDate != null) {
                            val endCalendar = Calendar.getInstance()
                            endCalendar.time = endTimeDate
                            if (endCalendar.before(calendar) || endCalendar.equals(calendar)) {
                                // Set end time to 30 minutes after start time
                                val newEndCalendar = Calendar.getInstance()
                                newEndCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                newEndCalendar.set(Calendar.MINUTE, minute + 30)
                                if (newEndCalendar.get(Calendar.MINUTE) >= 60) {
                                    newEndCalendar.add(Calendar.HOUR_OF_DAY, 1)
                                    newEndCalendar.set(Calendar.MINUTE, newEndCalendar.get(Calendar.MINUTE) - 60)
                                }
                                endTimeInput.setText(timeFormat.format(newEndCalendar.time))
                            }
                        }
                    }
                },
                today.get(Calendar.HOUR_OF_DAY),
                today.get(Calendar.MINUTE),
                true // Use 24-hour format
            )
            timePickerDialog.show()
        }

        // End time picker
        endTimeInput.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    endTimeInput.setText(timeFormat.format(calendar.time))
                },
                today.get(Calendar.HOUR_OF_DAY),
                today.get(Calendar.MINUTE),
                true // Use 24-hour format
            )
            timePickerDialog.show()
        }

        // Cancel button
        dialogView.findViewById<View>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        // Add button
        dialogView.findViewById<View>(R.id.addButton).setOnClickListener {
            val title = titleInput.text.toString().trim()
            val date = dateInput.text.toString()
            val startTime = startTimeInput.text.toString()
            val endTime = endTimeInput.text.toString()
            val cost = costInput.text.toString().toDoubleOrNull() ?: 0.0

            if (title.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                // Show error
                return@setOnClickListener
            }

            // Parse times and create task
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val startTimeDate = timeFormat.parse(startTime)
            val endTimeDate = timeFormat.parse(endTime)
            
            if (startTimeDate != null && endTimeDate != null) {
                val startCalendar = Calendar.getInstance()
                startCalendar.time = startTimeDate
                
                val endCalendar = Calendar.getInstance()
                endCalendar.time = endTimeDate
                
                // If end time is before or equal to start time, assume it crosses midnight (+1 day)
                if (!endCalendar.after(startCalendar)) {
                    endCalendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                val duration = ((endCalendar.timeInMillis - startCalendar.timeInMillis) / (1000 * 60)).toLong()
                
                val task = Task(
                    planId = planId,
                    title = title,
                    startTime = startCalendar.time,
                    endTime = endCalendar.time,
                    duration = duration,
                    cost = cost,
                    date = date,
                    isFixedStartTime = fixedStartTimeCheckbox.isChecked
                )
                
                addTask(task)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun addTask(task: Task) {
        val taskId = database.insertTask(task)
        if (taskId > 0) {
            tasks.add(task.copy(id = taskId))
            updateUI()
        }
    }

    private fun removeTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Remove Task")
            .setMessage("Are you sure you want to remove '${task.title}'?")
            .setPositiveButton("Remove") { _, _ ->
                val deleted = database.deleteTask(task.id)
                if (deleted > 0) {
                    tasks.removeAll { it.id == task.id }
                    updateUI()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Update dialog title and button text for edit mode
        dialogView.findViewById<TextView>(R.id.dialogTitle).text = "Edit Task"
        val addButton = dialogView.findViewById<android.widget.Button>(R.id.addButton)
        addButton.text = "Save"

        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.taskTitleInput)
        val dateInput = dialogView.findViewById<TextInputEditText>(R.id.dateInput)
        val startTimeInput = dialogView.findViewById<TextInputEditText>(R.id.startTimeInput)
        val endTimeInput = dialogView.findViewById<TextInputEditText>(R.id.endTimeInput)
        val costInput = dialogView.findViewById<TextInputEditText>(R.id.costInput)
        val fixedStartTimeCheckbox = dialogView.findViewById<android.widget.CheckBox>(R.id.fixedStartTimeCheckbox)

        // Pre-fill fields with existing task data
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        titleInput.setText(task.title)
        dateInput.setText(task.date)
        startTimeInput.setText(timeFormat.format(task.startTime))
        endTimeInput.setText(timeFormat.format(task.endTime))
        costInput.setText(String.format(Locale.getDefault(), "%.2f", task.cost))
        fixedStartTimeCheckbox.isChecked = task.isFixedStartTime

        // Parse trip start and end dates
        val tripStartDate = parseTripDate(planStartDate)
        val tripEndDate = parseTripDate(planEndDate)

        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Date picker with trip date range restrictions
        dateInput.setOnClickListener {
            val currentDate = try {
                val d = dateFormat.parse(dateInput.text.toString())
                Calendar.getInstance().apply { if (d != null) time = d }
            } catch (e: Exception) {
                tripStartDate ?: Calendar.getInstance()
            }

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)

                    if (tripStartDate != null && tripEndDate != null) {
                        if (selectedCalendar.before(tripStartDate) || selectedCalendar.after(tripEndDate)) {
                            Toast.makeText(this, "Date must be within trip dates: ${dateFormat.format(tripStartDate.time)} to ${dateFormat.format(tripEndDate.time)}", Toast.LENGTH_LONG).show()
                            return@DatePickerDialog
                        }
                    }

                    dateInput.setText(dateFormat.format(selectedCalendar.time))
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
            )

            if (tripStartDate != null && tripEndDate != null) {
                datePickerDialog.datePicker.minDate = tripStartDate.timeInMillis
                datePickerDialog.datePicker.maxDate = tripEndDate.timeInMillis
            }

            datePickerDialog.show()
        }

        // Start time picker
        startTimeInput.setOnClickListener {
            val currentStartTime = try {
                val d = timeFormat.parse(startTimeInput.text.toString())
                Calendar.getInstance().apply { if (d != null) time = d }
            } catch (e: Exception) {
                today
            }

            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    startTimeInput.setText(timeFormat.format(calendar.time))

                    val endTimeText = endTimeInput.text.toString()
                    if (endTimeText.isNotEmpty()) {
                        val endTimeDate = timeFormat.parse(endTimeText)
                        if (endTimeDate != null) {
                            val endCalendar = Calendar.getInstance()
                            endCalendar.time = endTimeDate
                            if (endCalendar.before(calendar) || endCalendar == calendar) {
                                val newEndCalendar = Calendar.getInstance()
                                newEndCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                newEndCalendar.set(Calendar.MINUTE, minute + 30)
                                if (newEndCalendar.get(Calendar.MINUTE) >= 60) {
                                    newEndCalendar.add(Calendar.HOUR_OF_DAY, 1)
                                    newEndCalendar.set(Calendar.MINUTE, newEndCalendar.get(Calendar.MINUTE) - 60)
                                }
                                endTimeInput.setText(timeFormat.format(newEndCalendar.time))
                            }
                        }
                    }
                },
                currentStartTime.get(Calendar.HOUR_OF_DAY),
                currentStartTime.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        // End time picker
        endTimeInput.setOnClickListener {
            val currentEndTime = try {
                val d = timeFormat.parse(endTimeInput.text.toString())
                Calendar.getInstance().apply { if (d != null) time = d }
            } catch (e: Exception) {
                today
            }

            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    endTimeInput.setText(timeFormat.format(calendar.time))
                },
                currentEndTime.get(Calendar.HOUR_OF_DAY),
                currentEndTime.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        // Cancel button
        dialogView.findViewById<View>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        // Save button
        addButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val date = dateInput.text.toString()
            val startTime = startTimeInput.text.toString()
            val endTime = endTimeInput.text.toString()
            val cost = costInput.text.toString().toDoubleOrNull() ?: 0.0

            if (title.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                return@setOnClickListener
            }

            val startTimeDate = timeFormat.parse(startTime)
            val endTimeDate = timeFormat.parse(endTime)

            if (startTimeDate != null && endTimeDate != null) {
                val startCalendar = Calendar.getInstance()
                startCalendar.time = startTimeDate

                val endCalendar = Calendar.getInstance()
                endCalendar.time = endTimeDate

                // If end time is before or equal to start time, assume it crosses midnight (+1 day)
                if (!endCalendar.after(startCalendar)) {
                    endCalendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                val duration = ((endCalendar.timeInMillis - startCalendar.timeInMillis) / (1000 * 60)).toLong()

                val updatedTask = task.copy(
                    title = title,
                    startTime = startCalendar.time,
                    endTime = endCalendar.time,
                    duration = duration,
                    cost = cost,
                    date = date,
                    isFixedStartTime = fixedStartTimeCheckbox.isChecked
                )

                updateTask(updatedTask)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun updateTask(task: Task) {
        val rowsUpdated = database.updateTask(task)
        if (rowsUpdated > 0) {
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                tasks[index] = task
                updateUI()
            }
        }
    }

    /**
     * Delay a task by the given number of minutes and cascade the delay
     * to all subsequent conflicting tasks on the same day.
     * If a fixed-start-time task would need to be moved, show an error instead.
     */
    private fun delayTask(task: Task, delayMinutes: Int) {
        val delayMillis = delayMinutes * 60 * 1000L

        // Sort tasks for the same day by start time
        val sameDayTasks = tasks
            .filter { it.date == task.date }
            .sortedBy { it.startTime }

        val taskIndex = sameDayTasks.indexOfFirst { it.id == task.id }
        if (taskIndex == -1) return

        // Calculate new end time for the delayed task (start time stays the same, end time shifts)
        val delayedTask = sameDayTasks[taskIndex]
        val newEndTime = Date(delayedTask.endTime.time + delayMillis)
        val newDuration = delayedTask.duration + delayMinutes

        // Build the list of tasks that need shifting (the ones after this task that conflict)
        val tasksToShift = mutableListOf<Pair<Task, Long>>() // task to shift amount in millis
        var currentPushEnd = newEndTime

        for (i in (taskIndex + 1) until sameDayTasks.size) {
            val nextTask = sameDayTasks[i]
            // Check if this task's start time conflicts with the pushed end time
            if (nextTask.startTime.before(currentPushEnd)) {
                // This task needs to be pushed
                if (nextTask.isFixedStartTime) {
                    // Conflict with a fixed-start-time task - show error and abort
                    AlertDialog.Builder(this)
                        .setTitle("Delay Conflict")
                        .setMessage("Cannot apply delay: '${nextTask.title}' has a fixed start time and would need to be moved. Please resolve this manually.")
                        .setPositiveButton("OK", null)
                        .show()
                    return
                }
                val shiftAmount = currentPushEnd.time - nextTask.startTime.time
                tasksToShift.add(nextTask to shiftAmount)
                currentPushEnd = Date(nextTask.endTime.time + shiftAmount)
            } else {
                // No more conflicts
                break
            }
        }

        // All clear — apply the delay to the original task
        val updatedDelayedTask = delayedTask.copy(
            endTime = newEndTime,
            duration = newDuration
        )
        database.updateTask(updatedDelayedTask)
        val delayedIndex = tasks.indexOfFirst { it.id == updatedDelayedTask.id }
        if (delayedIndex != -1) tasks[delayedIndex] = updatedDelayedTask

        // Apply shifts to subsequent tasks
        for ((shiftTask, shiftAmount) in tasksToShift) {
            val shiftedTask = shiftTask.copy(
                startTime = Date(shiftTask.startTime.time + shiftAmount),
                endTime = Date(shiftTask.endTime.time + shiftAmount)
            )
            database.updateTask(shiftedTask)
            val idx = tasks.indexOfFirst { it.id == shiftedTask.id }
            if (idx != -1) tasks[idx] = shiftedTask
        }

        updateUI()

        val totalShifted = tasksToShift.size
        val message = if (totalShifted > 0) {
            "Delayed '${task.title}' by $delayMinutes min and shifted $totalShifted subsequent task(s)"
        } else {
            "Delayed '${task.title}' by $delayMinutes min"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Parse trip date string to Calendar object
     * Handles both "EEE MMM dd yyyy" format (from plan creation) and "yyyy-MM-dd" format
     */
    private fun parseTripDate(dateString: String): Calendar? {
        if (dateString.isEmpty()) return null
        
        // Try parsing with "EEE MMM dd yyyy" format first (from plan creation)
        try {
            val dateFormat1 = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())
            val date1 = dateFormat1.parse(dateString)
            if (date1 != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date1
                return calendar
            }
        } catch (e: Exception) {
            // Try parsing with "yyyy-MM-dd" format
            try {
                val dateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date2 = dateFormat2.parse(dateString)
                if (date2 != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date2
                    return calendar
                }
            } catch (e2: Exception) {
                // If both fail, return null
            }
        }
        
        return null
    }
    
    /**
     * Calculate total cost from all tasks in the current plan
     */
    private fun calculateTotalCost(): Double {
        return tasks.sumOf { it.cost }
    }
    
    /**
     * Update the cost display in the plan dates text
     */
    private fun updateCostDisplay() {
        val totalCost = calculateTotalCost()
        val costFormat = String.format(Locale.getDefault(), "%.2f", totalCost)
        
        // Format dates for display
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val startDate = try {
            SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault()).parse(planStartDate)
        } catch (e: Exception) {
            null
        }
        val endDate = try {
            SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault()).parse(planEndDate)
        } catch (e: Exception) {
            null
        }
        
        val formattedStartDate = startDate?.let { dateFormat.format(it) } ?: planStartDate
        val formattedEndDate = endDate?.let { dateFormat.format(it) } ?: planEndDate
        
        val currencySymbol = MainActivity.getCurrencySymbol(this)
        planDates.text = "$formattedStartDate - $formattedEndDate • $currencySymbol$costFormat"
    }
} 