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

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.pixelarry.itinerary_planner.data.PlansRepository
import com.pixelarry.itinerary_planner.ui.PlansAdapter
import com.pixelarry.itinerary_planner.ui.PlanUiModel

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        setupEdgeToEdge(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_about -> openUrl("https://sanandmv7.github.io/apps/itinerary-planner/")
                R.id.nav_bug -> openUrl("https://github.com/pixelarry/itinerary_planner/issues/new")
                R.id.nav_feedback -> openUrl("https://forms.gle/qKYahftqpdnrLhZ49")
                R.id.nav_support -> openUrl("https://ko-fi.com/sanandmv")
                R.id.nav_libraries -> {
                    val intent = Intent(this, OpenSourceLibrariesActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Set app version in navigation header
        val headerView = navigationView.getHeaderView(0)
        val versionTextView = headerView.findViewById<TextView>(R.id.nav_header_version)
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionTextView.text = "Version ${packageInfo.versionName}"
        } catch (e: PackageManager.NameNotFoundException) {
            versionTextView.text = "Version 1.0"
        }

        val titleView = findViewById<TextView>(R.id.greetingTitle)
        titleView.text = generateGreeting()

        val recycler = findViewById<RecyclerView>(R.id.plansRecycler)
        val emptyView = findViewById<TextView>(R.id.emptyState)
        recycler.layoutManager = LinearLayoutManager(this)

        fun loadPlans() {
            val repository = PlansRepository(this)
            val plans = sortPlansByStartDate(repository.getPlans())
            recycler.adapter = PlansAdapter(plans, 
                onPlanClicked = { plan ->
                    // Navigate to plan details
                    val intent = Intent(this@MainActivity, PlanDetailsActivity::class.java).apply {
                        putExtra("plan_id", plan.id)
                        putExtra("plan_title", plan.title)
                        putExtra("plan_start_date", plan.startDate)
                        putExtra("plan_end_date", plan.endDate)
                        putExtra("plan_image_url", plan.imageUrl)
                    }
                    startActivity(intent)
                },
                onPlanLongClicked = { plan ->
                    showDeleteConfirmationDialog(plan, repository)
                }
            )
            emptyView.visibility = if (plans.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
        loadPlans()

        val fab = findViewById<FloatingActionButton>(R.id.addPlanFab)
        fab.setOnClickListener {
            startActivityForResult(Intent(this, NewPlanActivity::class.java), REQ_NEW_PLAN)
        }
    }

    private fun setupEdgeToEdge(toolbar: MaterialToolbar) {
        toolbar.applySystemBarsPadding(applyTop = true)

        val recycler = findViewById<RecyclerView>(R.id.plansRecycler)
        val emptyView = findViewById<TextView>(R.id.emptyState)
        val fab = findViewById<FloatingActionButton>(R.id.addPlanFab)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        recycler.applySystemBarsPadding(applyBottom = true)
        emptyView.applySystemBarsPadding(applyBottom = true)
        fab.applySystemBarsMargins(applyRight = true, applyBottom = true)
        navigationView.applySystemBarsPadding(applyTop = true, applyBottom = true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun generateGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning!"
            in 12..17 -> "Good Afternoon!"
            else -> "Good Evening!"
        }
    }

    private fun sortPlansByStartDate(plans: List<PlanUiModel>): List<PlanUiModel> {
        return plans.sortedBy { plan ->
            try {
                val dateFormat = java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.getDefault())
                dateFormat.parse(plan.startDate)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }

    private fun showDeleteConfirmationDialog(plan: PlanUiModel, repository: PlansRepository) {
        AlertDialog.Builder(this)
            .setTitle("Delete Plan")
            .setMessage("Are you sure you want to delete this plan?")
            .setPositiveButton("Delete") { _, _ ->
                val deletedRows = repository.deletePlan(plan.id)
                if (deletedRows > 0) {
                    Toast.makeText(this, "Plan deleted successfully", Toast.LENGTH_SHORT).show()
                    refreshPlans()
                } else {
                    Toast.makeText(this, "Failed to delete plan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshPlans() {
        val recycler = findViewById<RecyclerView>(R.id.plansRecycler)
        val emptyView = findViewById<TextView>(R.id.emptyState)
        val repository = PlansRepository(this)
        val plans = sortPlansByStartDate(repository.getPlans())
        recycler.adapter = PlansAdapter(plans,
            onPlanClicked = { plan ->
                // Navigate to plan details
                val intent = Intent(this@MainActivity, PlanDetailsActivity::class.java).apply {
                    putExtra("plan_id", plan.id)
                    putExtra("plan_title", plan.title)
                    putExtra("plan_start_date", plan.startDate)
                    putExtra("plan_end_date", plan.endDate)
                    putExtra("plan_image_url", plan.imageUrl)
                }
                startActivity(intent)
            },
            onPlanLongClicked = { plan ->
                showDeleteConfirmationDialog(plan, repository)
            }
        )
        emptyView.visibility = if (plans.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_NEW_PLAN && resultCode == Activity.RESULT_OK) {
            val recycler = findViewById<RecyclerView>(R.id.plansRecycler)
            val emptyView = findViewById<TextView>(R.id.emptyState)
            val repository = PlansRepository(this)
            val plans = sortPlansByStartDate(repository.getPlans())
            recycler.adapter = PlansAdapter(plans,
                onPlanClicked = { plan ->
                    // Navigate to plan details
                    val intent = Intent(this@MainActivity, PlanDetailsActivity::class.java).apply {
                        putExtra("plan_id", plan.id)
                        putExtra("plan_title", plan.title)
                        putExtra("plan_start_date", plan.startDate)
                        putExtra("plan_end_date", plan.endDate)
                        putExtra("plan_image_url", plan.imageUrl)
                    }
                    startActivity(intent)
                },
                onPlanLongClicked = { plan ->
                    showDeleteConfirmationDialog(plan, repository)
                }
            )
            emptyView.visibility = if (plans.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQ_NEW_PLAN = 1001
    }
}

