package si.uni_lj.fri.pbd.classproject2.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import si.uni_lj.fri.pbd.classproject2.services.SensingService

class ActivityRecognitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val mostProbableActivity = result?.mostProbableActivity
            val type = mostProbableActivity?.type

            val activityStr = when (type) {
                DetectedActivity.IN_VEHICLE -> "In Vehicle"
                DetectedActivity.ON_BICYCLE -> "On Bicycle"
                DetectedActivity.ON_FOOT -> "On Foot"
                DetectedActivity.RUNNING -> "Running"
                DetectedActivity.STILL -> "Still"
                DetectedActivity.TILTING -> "Tilting"
                DetectedActivity.WALKING -> "Walking"
                DetectedActivity.UNKNOWN -> "Unknown"
                else -> "Unidentified"
            }

            val serviceIntent = Intent(context, SensingService::class.java)
            serviceIntent.putExtra("activity", activityStr)
            context.startService(serviceIntent)
        }
    }
}