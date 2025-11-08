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

package com.pixelarry.itinerary_planner

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.pixelarry.itinerary_planner.data.PlansRepository
import com.pixelarry.itinerary_planner.ui.PlanUiModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewPlanActivity : AppCompatActivity() {
    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())
    private var selectedImageUri: Uri? = null
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_plan)

        setupEdgeToEdge()

        val inputTitle = findViewById<TextInputEditText>(R.id.inputTitle)
        val inputStart = findViewById<TextInputEditText>(R.id.inputStartDate)
        val inputEnd = findViewById<TextInputEditText>(R.id.inputEndDate)
        val btnCreate = findViewById<MaterialButton>(R.id.btnCreate)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val selectArea = findViewById<LinearLayout>(R.id.selectImageArea)
        val imagePreview = findViewById<ImageView>(R.id.imagePreview)
        val selectText = findViewById<TextView>(R.id.selectImageText)

        fun pickDate(target: TextView, isStartDate: Boolean) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->
                val c = Calendar.getInstance()
                c.set(y, m, d)
                
                if (isStartDate) {
                    startDate = c
                    // Reset end date if it's before the new start date
                    if (endDate != null && c.after(endDate)) {
                        endDate = null
                        inputEnd.text?.clear()
                        Toast.makeText(this, "End date cleared as it was before the new start date", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Check if end date is before start date
                    if (startDate != null && c.before(startDate)) {
                        Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show()
                        return@DatePickerDialog
                    }
                    endDate = c
                }
                
                target.text = dateFormat.format(c.time)
            }, year, month, day)
            
            // Set minimum date for end date picker
            if (!isStartDate && startDate != null) {
                datePickerDialog.datePicker.minDate = startDate!!.timeInMillis
            }
            
            datePickerDialog.show()
        }

        btnBack.setOnClickListener { finish() }
        
        inputStart.setOnClickListener { pickDate(inputStart, true) }
        inputEnd.setOnClickListener { pickDate(inputEnd, false) }

        selectArea.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, REQ_PICK_IMAGE)
        }

        btnCreate.setOnClickListener {
            val title = inputTitle.text?.toString()?.trim().orEmpty()
            val start = inputStart.text?.toString()?.trim().orEmpty()
            val end = inputEnd.text?.toString()?.trim().orEmpty()

            if (title.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Additional validation: ensure end date is not before start date
            if (startDate != null && endDate != null && endDate!!.before(startDate)) {
                Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val repo = PlansRepository(this)
            repo.addPlan(
                PlanUiModel(
                    title = title,
                    startDate = start,
                    endDate = end,
                    imageUrl = selectedImageUri?.toString() ?: ""
                )
            )
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setupEdgeToEdge() {
        val scrollView = findViewById<ScrollView>(R.id.newPlanScroll)
        scrollView.applySystemBarsPadding(applyTop = true, applyBottom = true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK) {
            val source = data?.data ?: return
            val destination = Uri.fromFile(java.io.File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
            UCrop.of(source, destination)
                .withAspectRatio(16f, 9f)
                .start(this)
            return
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            if (resultUri != null) {
                selectedImageUri = resultUri
                val preview = findViewById<ImageView>(R.id.imagePreview)
                preview.imageTintList = null
                preview.clearColorFilter()
                Glide.with(this)
                    .load(resultUri)
                    .centerCrop()
                    .into(preview)
                findViewById<TextView>(R.id.selectImageText).visibility = android.view.View.GONE
            }
            return
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "Couldn't crop image", Toast.LENGTH_SHORT).show()
            return
        }
    }

    companion object {
        private const val REQ_PICK_IMAGE = 2001
    }
}

