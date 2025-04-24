package si.uni_lj.fri.pbd.classproject2.fragments

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import java.time.LocalTime
import java.util.Locale
import androidx.core.content.edit
import si.uni_lj.fri.pbd.classproject2.R
import si.uni_lj.fri.pbd.classproject2.services.SensingService

class SettingsFragment : PreferenceFragmentCompat() {

    private val pref by lazy {
        preferenceManager.sharedPreferences
    }

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value } // Check if all permissions are granted
            if (allGranted) {
                startSensingService()
            } else {
                Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
                val trackingSwitch = findPreference<SwitchPreferenceCompat>("tracking_enabled")
                trackingSwitch?.isChecked = false
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val trackingSwitch = findPreference<SwitchPreferenceCompat>("tracking_enabled")
        trackingSwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) requestPermissions()
            else stopSensingService()
            true
        }

        val notificationTimePref = findPreference<Preference>("notification_time")
        notificationTimePref?.setOnPreferenceClickListener {
            showTimePicker()
            true
        }

        val resetPref = findPreference<Preference>("reset_app")
        resetPref?.setOnPreferenceClickListener {
            resetApp()
            true
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else startSensingService()
    }


    private fun startSensingService() {
        Log.d("SettingsFragment", "Starting sensing service.")
        val serviceIntent = Intent(requireContext(), SensingService::class.java)
        // serviceIntent.action = SensingService.ACTION_START_TRACKING
        requireContext().startForegroundService(serviceIntent)
    }

    private fun stopSensingService() {
        Log.d("SettingsFragment", "Stopping sensing service.")
        val serviceIntent = Intent(requireContext(), SensingService::class.java)
        // serviceIntent.action = SensingService.ACTION_STOP_TRACKING
        requireContext().stopService(serviceIntent)
    }

    private fun showTimePicker() {
        val now = LocalTime.now()
        val picker = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            val time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            pref?.edit { putString("notification_time", time) }
            findPreference<Preference>("notification_time")?.summary = time
        }, now.hour, now.minute, true)
        picker.show()
    }

    private fun resetApp() {
        pref?.edit { clear() }
        Toast.makeText(requireContext(), "App reset", Toast.LENGTH_SHORT).show()
    }
}