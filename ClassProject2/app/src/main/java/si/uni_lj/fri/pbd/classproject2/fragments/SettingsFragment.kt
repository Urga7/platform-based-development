package si.uni_lj.fri.pbd.classproject2.fragments

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
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.requiredPermissions
import com.google.android.material.snackbar.Snackbar
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.getStepGoal
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.scheduleAlarm

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val TRACKING_SWITCH = "tracking_enabled"
        const val NOTIFICATION_TIME = "notification_time"
        const val RESET_APP = "reset_app"
    }

    private val pref by lazy {
        preferenceManager.sharedPreferences
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                startSensingService()
            } else {
                Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
                val trackingSwitch = findPreference<SwitchPreferenceCompat>(TRACKING_SWITCH)
                trackingSwitch?.isChecked = false
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val trackingSwitch = findPreference<SwitchPreferenceCompat>(TRACKING_SWITCH)
        trackingSwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) requestPermissions()
            else stopSensingService()
            true
        }

        val notificationTimePref = findPreference<Preference>(NOTIFICATION_TIME)
        notificationTimePref?.apply {
            val savedTime = pref?.getString(NOTIFICATION_TIME, null)
            summary = savedTime ?: "Select a time for daily notification"

            setOnPreferenceClickListener {
                showTimePicker()
                true
            }
        }

        val resetPref = findPreference<Preference>(RESET_APP)
        resetPref?.setOnPreferenceClickListener {
            resetApp()
            true
        }
    }

    private fun startSensingService() {
        Log.d("SettingsFragment", "Starting sensing service.")
        val serviceIntent = Intent(requireContext(), SensingService::class.java)
        requireContext().startForegroundService(serviceIntent)
    }

    private fun stopSensingService() {
        Log.d("SettingsFragment", "Stopping sensing service.")
        val serviceIntent = Intent(requireContext(), SensingService::class.java)
        requireContext().stopService(serviceIntent)
    }

    private fun requestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else startSensingService()
    }

    private fun showTimePicker() {
        val now = LocalTime.now()
        val picker = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            if (getStepGoal(requireContext()) == -1) {
                Snackbar.make(
                    requireView(),
                    "You must set your step goal before setting the notification time",
                    Snackbar.LENGTH_LONG
                ).show()
                return@TimePickerDialog
            }

            val selectedTime = LocalTime.of(hourOfDay, minute)
            val timeString = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            pref?.edit { putString(NOTIFICATION_TIME, timeString) }
            findPreference<Preference>(NOTIFICATION_TIME)?.summary = timeString
            scheduleAlarm(requireContext(), selectedTime)
        }, now.hour, now.minute, true)
        picker.show()
    }

    private fun resetApp() {
        pref?.edit { clear() }

        // Update the summary for the Notification Time preference
        val notificationTimePref = findPreference<Preference>(NOTIFICATION_TIME)
        notificationTimePref?.summary = "Select a time for daily notification"

        Toast.makeText(requireContext(), "App reset", Toast.LENGTH_SHORT).show()
    }
}