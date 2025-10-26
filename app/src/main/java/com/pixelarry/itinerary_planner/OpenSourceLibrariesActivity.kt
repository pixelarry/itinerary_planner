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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.mikepenz.aboutlibraries.LibsBuilder

class OpenSourceLibrariesActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val toolbar = MaterialToolbar(this)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Open Source Libraries"
        
        // Use AboutLibraries to automatically display all dependencies
        LibsBuilder()
            .withActivityTitle("Open Source Libraries")
            .withAboutIconShown(true)
            .withAboutVersionShown(true)
            .withAboutDescription("This app uses the following open source libraries:")
            .withLicenseShown(true)
            .withLicenseDialog(true)
            .withVersionShown(true)
            .withSortEnabled(true)
            .start(this)
        // Finish this wrapper activity so back goes straight to home
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
