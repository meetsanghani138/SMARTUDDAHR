package com.marwadiuniversity.radhe.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.marwadiuniversity.radhe.R
import com.marwadiuniversity.radhe.activities.auth.LoginActivity
import com.marwadiuniversity.radhe.activities.auth.ShopSetupActivity
import com.marwadiuniversity.radhe.activities.staff.StaffManagementActivity
import com.marwadiuniversity.radhe.databinding.FragmentSettingsBinding
import com.marwadiuniversity.radhe.utils.BackupRestoreManager
import com.marwadiuniversity.radhe.utils.DeveloperSecretHelper
import com.marwadiuniversity.radhe.utils.SessionManager

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    private var secretTapCount = 0
    private var secretLastTapTime = 0L
    private val secretHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupClickListeners()
        setupSecretGesture()
    }

    private fun setupSecretGesture() {
        binding.tvSecretDevCreditSettings.visibility = View.GONE

        val secretTapListener = View.OnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - secretLastTapTime < 800) {
                secretTapCount++
            } else {
                secretTapCount = 1
            }
            secretLastTapTime = currentTime

            if (secretTapCount >= 2) {
                secretTapCount = 0
                binding.tvSecretDevCreditSettings.visibility = View.VISIBLE
                val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.splash_fade_in)
                binding.tvSecretDevCreditSettings.startAnimation(fadeIn)

                secretHandler.postDelayed({
                    if (_binding != null) {
                        binding.tvSecretDevCreditSettings.visibility = View.GONE
                    }
                }, 3000)
            }
        }

        binding.tvSettingsHeaderTitle.setOnClickListener(secretTapListener)
    }

    private fun setupClickListeners() {
        binding.btnEditShopProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ShopSetupActivity::class.java))
        }

        binding.btnChangeLanguage.setOnClickListener {
            showLanguageDialog()
        }

        binding.btnStaffManagement.setOnClickListener {
            startActivity(Intent(requireContext(), StaffManagementActivity::class.java))
        }

        binding.btnBackupRestore.setOnClickListener {
            showBackupDialog()
        }

        binding.btnDeveloperCredits.setOnClickListener {
            showDeveloperCreditsDialog()
        }

        binding.btnAboutApp.setOnClickListener {
            showAboutAppDialog()
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun showDeveloperCreditsDialog() {
        DeveloperSecretHelper.showDeveloperCreditsDialog(requireActivity())
    }

    private fun showAboutAppDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About Smart Udhar")
            .setMessage(
                """
                📱 Smart Udhar v1.0.0
                "Smart Hisab, Easy Vyapar"
                
                A digital ledger & credit accounting application built for pan shops and Kirana retailers.
                
                Features:
                • Customer Udhar Management
                • Payment Collection & Ledger
                • Product Inventory
                • Native PDF Receipts & WhatsApp Reminders
                • Offline-first SQLite database
                """.trimIndent()
            )
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "ગુજરાતી (Gujarati)", "हिंदी (Hindi)")
        val codes = arrayOf("en", "gu", "hi")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                val selected = codes[which]
                sessionManager.setLanguage(selected)
                Toast.makeText(requireContext(), "Language set to ${languages[which]}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBackupDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Backup & Restore")
            .setMessage("Export database backup as a JSON file to share or store safely.")
            .setPositiveButton("Export Backup") { _, _ ->
                val backupFile = BackupRestoreManager.createBackupJson(requireContext())
                if (backupFile != null && backupFile.exists()) {
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider",
                        backupFile
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Export Smart Udhar Backup"))
                } else {
                    Toast.makeText(requireContext(), "Backup failed!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
