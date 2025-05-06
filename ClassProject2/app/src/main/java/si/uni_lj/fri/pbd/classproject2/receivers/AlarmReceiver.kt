package si.uni_lj.fri.pbd.classproject2.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import si.uni_lj.fri.pbd.classproject2.MainActivity
import si.uni_lj.fri.pbd.classproject2.R
import android.Manifest
import android.content.pm.PackageManager
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.STEP_COUNT_NOTIFICATION_CHANNEL_ID
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.STEP_COUNT_NOTIFICATION_ID
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.TAG_ALARM_RECEIVER
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.scheduleAlarm
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.getNotificationTime
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.getStepCount
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.getStepGoal
import java.time.format.DateTimeFormatter

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG_ALARM_RECEIVER, "Alarm triggered!")
        if (context == null) {
            Log.d(TAG_ALARM_RECEIVER, "Context is null")
            return
        }

        sendStepGoalNotification(context)
        scheduleNextAlarm(context)
    }

    private fun sendStepGoalNotification(context: Context) {
        val stepsTaken = getStepCount(context)
        val stepGoal = getStepGoal(context)
        val stepsRemaining = stepGoal - stepsTaken
        Log.d(TAG_ALARM_RECEIVER, "stepsTaken: $stepsTaken, stepGoal: $stepGoal")

        val notificationText = if (stepsTaken < stepGoal) {
            "You haven't achieved your step count goal for today! You took $stepsTaken and your goal is $stepGoal. Take $stepsRemaining more steps to complete your goal."
        } else {
            "Congratulations! You have achieved your step count goal for today. You took $stepsTaken steps."
        }

        createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, STEP_COUNT_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Step Goal Update")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(STEP_COUNT_NOTIFICATION_ID, builder.build())
            } else {
                Log.e(TAG_ALARM_RECEIVER, "Notification permission not granted")
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        val name = "Step Goal Notifications"
        val descriptionText = "Daily notifications about your step goal progress"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(STEP_COUNT_NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleNextAlarm(context: Context) {
        val nextAlarmTime = getNotificationTime(context)
        if (nextAlarmTime == null) {
            Log.d(TAG_ALARM_RECEIVER, "Could not fetch notification time")
            return
        }

        scheduleAlarm(context, nextAlarmTime, false)
        Log.d("AlarmReceiver", "Rescheduled alarm for tomorrow at ${nextAlarmTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
    }
}