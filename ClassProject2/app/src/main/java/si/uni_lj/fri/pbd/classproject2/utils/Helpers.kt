package si.uni_lj.fri.pbd.classproject2.utils

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.widget.Toast
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.location.DetectedActivity
import si.uni_lj.fri.pbd.classproject2.MainActivity
import si.uni_lj.fri.pbd.classproject2.receivers.AlarmReceiver
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object Helpers {
    const val TAG_HELPERS = "Helpers"

    const val KEY_NOTIFICATION_TIME = "notification_time"
    const val KEY_STEP_GOAL = "step_goal"
    const val ALARM_REQUEST_CODE = 0
    const val ACTION_UPDATE_ACTIVITY = "si.uni_lj.fri.pbd.classproject2.UPDATE_ACTIVITY"
    const val EXTRA_ACTIVITY_NAME = "extra_activity_name"
    const val TAG_DASHBOARD = "DashboardFragment"
    const val TAG_ALARM_RECEIVER = "AlarmReceiver"
    const val TAG_SENSING_SERVICE = "SensingService"
    const val STEP_PREFS_NAME = "StepPrefs"
    const val STEP_COUNT_NOTIFICATION_CHANNEL_ID = "step_notification_channel"
    const val STEP_COUNT_NOTIFICATION_ID = 100

    val requiredPermissions = arrayOf(
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.POST_NOTIFICATIONS
    )

    fun getDailyStepCountKey(): String {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH) + 1 // Month is 0-indexed
        val day = now.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "step_count_%04d_%02d_%02d", year, month, day)
    }

    fun activityToString(type: Int): String {
        return when (type) {
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            else -> "UNDEFINED"
        }
    }

    fun getTodayKey(): String {
        val now = java.util.Calendar.getInstance()
        val y = now.get(java.util.Calendar.YEAR)
        val m = now.get(java.util.Calendar.MONTH) + 1
        val d = now.get(java.util.Calendar.DAY_OF_MONTH)
        return "$y-$m-$d"
    }

    fun createNotificationChannel(context: Context, channelId: String, channelName: String) {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun buildDashboardIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("openDashboard", true)
        }

        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun scheduleAlarm(context: Context, time: LocalTime, cancelExisting: Boolean = true) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (cancelExisting) {
            val alarmUp = (PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) != null)

            if (alarmUp) {
                Log.d(TAG_ALARM_RECEIVER, "Alarm is already scheduled. Cancelling existing alarm.")
                alarmManager.cancel(pendingIntent)
            }
        }

        val now = LocalDateTime.now()
        val alarmDateTime = now.withHour(time.hour).withMinute(time.minute).withSecond(0)

        val triggerAtMillis = if (alarmDateTime.isBefore(now)) {
            alarmDateTime.plusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else {
            alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            if (!cancelExisting)
                Toast.makeText(context, "Alarm set for ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}", Toast.LENGTH_SHORT).show()
        } else if (!cancelExisting) {
            Toast.makeText(context, "Cannot schedule exact alarm", Toast.LENGTH_SHORT).show()
        }
    }

    fun getStepCount(context: Context): Int {
        val sharedPreferencesSteps = context.getSharedPreferences(STEP_PREFS_NAME, Context.MODE_PRIVATE)
        val stepsTaken = sharedPreferencesSteps.getInt(getDailyStepCountKey(), 0)
        return stepsTaken
    }

    fun getStepGoal(context: Context): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val stepGoalString = sharedPreferences.getString(KEY_STEP_GOAL, "-1")
        val stepGoal = stepGoalString?.toIntOrNull() ?: -1
        return stepGoal
    }

    fun getNotificationTime(context: Context): LocalTime? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val notificationTimeString = sharedPreferences.getString(KEY_NOTIFICATION_TIME, null) ?: return null
        val parts = notificationTimeString.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val notificationTime = LocalTime.of(hour, minute)
            return notificationTime
        } else return null
    }
}