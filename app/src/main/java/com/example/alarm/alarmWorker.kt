package com.example.alarm



import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class AlarmWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Afficher la notification avec le son de l'alarme
        showNotification()

        // Lire le son de l'alarme
        playAlarmSound()

        return Result.success()
    }

    private fun showNotification() {
        val notification = NotificationCompat.Builder(applicationContext, "alarm_channel")
            .setSmallIcon(R.drawable.baseline_access_alarm_24)
            .setContentTitle("Alarm")
            .setContentText("Time to wake up!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(android.provider.Settings.System.DEFAULT_RINGTONE_URI)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(1, notification)
    }

    private fun playAlarmSound() {
        val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.son)
        mediaPlayer.start()
    }

}
