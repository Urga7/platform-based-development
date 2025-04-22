package si.uni_lj.fri.pbd.classproject2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (!ActivityRecognitionResult.hasResult(intent)) {
            return
        }

        val result = ActivityRecognitionResult.extractResult(intent!!)
        val mostProbableActivity = result?.mostProbableActivity

        val type = when (mostProbableActivity?.type) {
            DetectedActivity.WALKING -> "Walking"
            DetectedActivity.RUNNING -> "Running"
            DetectedActivity.STILL -> "Still"
            DetectedActivity.IN_VEHICLE -> "In Vehicle"
            DetectedActivity.ON_BICYCLE -> "On Bicycle"
            else -> "Unknown"
        }

        Log.d("ActivityRecognition", "Detected activity: $type with confidence ${mostProbableActivity?.confidence}")
    }
}
