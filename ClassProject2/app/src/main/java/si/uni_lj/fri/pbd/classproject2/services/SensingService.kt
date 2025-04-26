package si.uni_lj.fri.pbd.classproject2.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import si.uni_lj.fri.pbd.classproject2.R
import si.uni_lj.fri.pbd.classproject2.receivers.ActivityRecognitionReceiver
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.ACTION_UPDATE_ACTIVITY
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.EXTRA_ACTIVITY_NAME
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.createNotificationChannel
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.buildDashboardIntent
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.TAG_SENSING_SERVICE
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.STEP_COUNT_NOTIFICATION_ID
import android.content.IntentFilter
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.getDailyStepCountKey
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

class SensingService : Service(), SensorEventListener {
    companion object {
        const val PREFS_NAME = "StepPrefs"
        const val KEY_BASELINE = "baseline"
        const val CHANNEL_NAME = "Sensing Service"
        const val CHANNEL_ID = "sensing_service_channel"
        private const val DUMMY_DATA_CREATED = "dummy_data_created"
    }

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var initialStepCount: Float? = null
    private var currentStepsToday: Int = 0

    private var currentActivity = ""
    private var isActivityReceiverRegistered = false
    private val activityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val activity = intent?.getStringExtra("activity") ?: return
            currentActivity = activity
            updateNotification()
            Log.d(TAG_SENSING_SERVICE, "Activity updated from broadcast: $currentActivity")
        }
    }

    private lateinit var client: ActivityRecognitionClient
    private lateinit var pendingIntent: PendingIntent

    private val handler = Handler(Looper.getMainLooper())
    private val hourlySaver = object : Runnable {
        override fun run() {
            saveStepCountToPrefs()
            handler.postDelayed(this, 3600000) // 1 hour in ms
        }
    }

    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    override fun onCreate() {
        super.onCreate()
        startForeground(STEP_COUNT_NOTIFICATION_ID, createNotification());

        val filter = IntentFilter("si.uni_lj.fri.pbd.classproject2.DETECTED_ACTIVITY")
        registerReceiver(activityReceiver, filter, RECEIVER_NOT_EXPORTED)
        isActivityReceiverRegistered = true
        Log.d(TAG_SENSING_SERVICE, "ActivityReceiver registered")

        client = ActivityRecognition.getClient(this)
        pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, ActivityRecognitionReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        client.requestActivityUpdates(
            5000,
            pendingIntent
        ).addOnSuccessListener {
            Log.d("ActivityRecognition", "Successfully requested activity updates")
        }.addOnFailureListener {
            Log.e("ActivityRecognition", "Failed to request activity updates", it)
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Log.w(TAG_SENSING_SERVICE, "Step sensor not available!")
            return
        }

        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d(TAG_SENSING_SERVICE, "Step sensor registered")

        addDummyStepDataForLastWeek()
    }

    override fun onDestroy() {
        super.onDestroy()

        sensorManager.unregisterListener(this)

        if (isActivityReceiverRegistered) {
            unregisterReceiver(activityReceiver)
            isActivityReceiverRegistered = false
            Log.d(TAG_SENSING_SERVICE, "ActivityReceiver unregistered")
        }

        handler.removeCallbacks(hourlySaver)
        Log.d(TAG_SENSING_SERVICE, "SensingService stopped")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (intent?.action == ACTION_UPDATE_ACTIVITY) {
            val detectedActivity = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
            if (detectedActivity != null) {
                currentActivity = detectedActivity
                updateNotification()
                Log.d(TAG_SENSING_SERVICE, "Updated current activity to: $currentActivity")
            }
        }

        handler.post(hourlySaver)
        Log.d(TAG_SENSING_SERVICE, "SensingService started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0] // since device boot
            if (initialStepCount == null) {
                initialStepCount = totalSteps
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                    putFloat(KEY_BASELINE, initialStepCount!!)
                }
                Log.d(TAG_SENSING_SERVICE, "Baseline step count set: $initialStepCount")
            }

            if (initialStepCount == null) return
            currentStepsToday = (totalSteps - initialStepCount!!).toInt()
            Log.d(TAG_SENSING_SERVICE, "Current steps today: $currentStepsToday")
            updateNotification()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun saveStepCountToPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(getDailyStepCountKey(), currentStepsToday) }
        Log.d(TAG_SENSING_SERVICE, "Step count saved to SharedPreferences: $currentStepsToday")
    }

    private fun createNotification(): Notification {
        createNotificationChannel(this, CHANNEL_ID, CHANNEL_NAME)
        return buildNotification()
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(STEP_COUNT_NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        var text = "Steps Today: $currentStepsToday"
        if (currentActivity.isNotEmpty()) text += ", Activity: $currentActivity"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Activity Tracker")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_baseline_directions_walk_24)
            .setContentIntent(buildDashboardIntent(this))
            .setOngoing(true)
            .build()
    }

    private fun addDummyStepDataForLastWeek() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Prevent adding dummy data multiple times
        if (sharedPreferences.getBoolean(DUMMY_DATA_CREATED, false)) {
            Log.d(TAG_SENSING_SERVICE, "Dummy step data already created.")
            return
        }

        val calendar = Calendar.getInstance()
        val random = Random(System.currentTimeMillis())
        sharedPreferences.edit {

            // Go back one day to exclude the current day
            calendar.add(Calendar.DAY_OF_MONTH, -1)

            for (i in 0..6) {
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val key = String.format(
                    Locale.getDefault(),
                    "step_count_%04d_%02d_%02d",
                    year,
                    month,
                    day
                )
                val dummySteps =
                    random.nextInt(5000, 15001) // Generate random steps between 5000 and 15000
                putInt(key, dummySteps)
                Log.d(
                    TAG_SENSING_SERVICE,
                    "Dummy data - Date: $year-$month-$day, Steps: $dummySteps, Key: $key"
                )
                calendar.add(Calendar.DAY_OF_MONTH, -1) // Go to the previous day
            }

            putBoolean(DUMMY_DATA_CREATED, true)
        }
        Log.d(TAG_SENSING_SERVICE, "Dummy step data for the last 7 days created.")
    }
}
