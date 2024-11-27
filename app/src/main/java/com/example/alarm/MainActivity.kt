package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()


        // Exemple d'un bouton qui permet de sélectionner l'heure pour l'alarme
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                scheduleAlarm(hourOfDay, minute)
            },
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            Calendar.getInstance().get(Calendar.MINUTE),
            true
        )

        timePickerDialog.show()
    }

    private fun scheduleAlarm(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DATE, 1) // Si l'heure est déjà passée, configure l'alarme pour demain
            }
        }

        val alarmTime = calendar.timeInMillis

        // Créer une demande de travail
        val workRequest = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            .setInitialDelay(alarmTime - System.currentTimeMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        // Planifier le travail avec WorkManager
        WorkManager.getInstance(this).enqueue(workRequest)
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

}
