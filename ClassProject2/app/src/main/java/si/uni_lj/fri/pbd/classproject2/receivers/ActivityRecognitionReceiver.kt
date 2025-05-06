package si.uni_lj.fri.pbd.classproject2.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.activityToString
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.getTodayKey
import androidx.core.content.edit
import org.json.JSONObject
import si.uni_lj.fri.pbd.classproject2.services.SensingService
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.ACTION_UPDATE_ACTIVITY
import si.uni_lj.fri.pbd.classproject2.utils.Helpers.EXTRA_ACTIVITY_NAME

class ActivityRecognitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val probableActivities = result?.probableActivities

            probableActivities?.maxByOrNull { it.confidence }?.let { mostProbableActivity ->
                val activityType = activityToString(mostProbableActivity.type)
                Log.d("ActivityRecognition", "Detected activity: $activityType")

                val broadcastIntent = Intent("si.uni_lj.fri.pbd.classproject2.DETECTED_ACTIVITY")
                broadcastIntent.putExtra("activity", activityType)
                context.sendBroadcast(broadcastIntent)

            }
        }
    }

    /*companion object {
        const val TAG = "ActivityRecognitionReceiver"
        const val PREFS_NAME = "activity_prefs"
        const val LAST_ACTIVITY_TYPE = "last_activity_type"
        const val LAST_ACTIVITY_TIMESTAMP = "last_activity_timestamp"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "ActivityRecognitionReceiver.onReceive() called")
        if (context == null || intent == null) return
        if (!ActivityRecognitionResult.hasResult(intent)) return

        val result = ActivityRecognitionResult.extractResult(intent) ?: return
        val mostProbable = result.mostProbableActivity
        val newType = mostProbable.type
        val confidence = mostProbable.confidence
        val newTypeString = activityToString(newType)
        Log.d(TAG, "Detected activity $newTypeString with confidence $confidence")
        if (confidence < 65) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        val lastType = prefs.getInt(LAST_ACTIVITY_TYPE, -1)
        val lastTimestamp = prefs.getLong(LAST_ACTIVITY_TIMESTAMP, now)
        val lastTypeString = activityToString(lastType)

        // Calculate duration spent in last activity
        val duration = now - lastTimestamp
        if (lastType != -1 && lastType != newType && duration > 0) {
            val key = "activity_durations_${getTodayKey()}"
            val rawJson = prefs.getString(key, "{}")
            val durations = JSONObject(rawJson ?: "{}")

            val previous = durations.optLong(lastTypeString, 0)
            durations.put(lastTypeString, previous + duration)

            prefs.edit { putString(key, durations.toString()) }

            Log.d(TAG, "Added ${duration / 1000}s to $lastTypeString")
        }

        // Log ENTER/EXIT
        if (newType != lastType) {
            if (lastType != -1) Log.d(TAG, "EXIT: $lastTypeString")
            Log.d(TAG, "ENTER: $newTypeString")
        }

        // Update prefs
        prefs.edit {
            putInt(LAST_ACTIVITY_TYPE, newType)
            putLong(LAST_ACTIVITY_TIMESTAMP, now)
        }

        // Broadcast the new activity
        val activityIntent = Intent(ACTIVITY_UPDATE_ACTION).apply {
            putExtra("activity_type", newType)
        }
        context.sendBroadcast(activityIntent)
    }*/
}
