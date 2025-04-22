package si.uni_lj.fri.pbd.classproject2

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager

import android.app.PendingIntent
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient

class SensingService : Service() {
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var pendingIntent: PendingIntent

    companion object {
        private const val CHANNEL_ID = "sensing_service_channel"
        private const val NOTIFICATION_ID = 1
    }

    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    override fun onCreate() {
        super.onCreate()
        activityRecognitionClient = ActivityRecognition.getClient(this)
        val intent = Intent(this, ActivityRecognitionReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        activityRecognitionClient.requestActivityUpdates(
            5000,
            pendingIntent
        ).addOnSuccessListener {
            Log.d("SensingService", "Activity updates requested successfully")
        }.addOnFailureListener {
            Log.e("SensingService", "Failed to request activity updates", it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Sensing Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sensing Active")
            .setContentText("Detecting activity...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

}
