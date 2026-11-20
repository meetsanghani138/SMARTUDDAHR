package com.marwadiuniversity.radhe.activities.staff

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.marwadiuniversity.radhe.adapters.StaffAdapter
import com.marwadiuniversity.radhe.databinding.ActivityStaffManagementBinding
import com.marwadiuniversity.radhe.repositories.StaffRepository

class StaffManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStaffManagementBinding
    private lateinit var staffRepo: StaffRepository
    private lateinit var adapter: StaffAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        staffRepo = StaffRepository(this)

        adapter = StaffAdapter(emptyList())
        binding.rvStaff.layoutManager = LinearLayoutManager(this)
        binding.rvStaff.adapter = adapter

        binding.fabAddStaff.setOnClickListener {
            showAddStaffDialog()
        }

        loadStaff()
    }

    private fun loadStaff() {
        val staffList = staffRepo.getStaffMembers()
        adapter.updateList(staffList)
    }

    private fun showAddStaffDialog() {
        AlertDialog.Builder(this)
            .setTitle("Add Staff Member")
            .setMessage("Default Manager role created")
            .setPositiveButton("Add Manager") { _, _ ->
                staffRepo.addStaff("New Staff", "9876543211", "MANAGER")
                Toast.makeText(this, "Staff added!", Toast.LENGTH_SHORT).show()
                loadStaff()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
