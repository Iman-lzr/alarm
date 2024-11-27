package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var alarmRecyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarms = mutableListOf<Alarm>()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val fab: FloatingActionButton = findViewById(R.id.fab)


        createNotificationChannel()


        sharedPreferences = getSharedPreferences("AlarmPreferences", MODE_PRIVATE)


        alarmRecyclerView = findViewById(R.id.recyclerView)
        alarmRecyclerView.layoutManager = LinearLayoutManager(this)


        alarmAdapter = AlarmAdapter(alarms, this)
        alarmRecyclerView.adapter = alarmAdapter


        loadAlarms()


        fab.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    showDaySelectionDialog(hourOfDay, minute)
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }
    }


    private fun showDaySelectionDialog(hour: Int, minute: Int) {
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val selectedDays = BooleanArray(daysOfWeek.size)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Days")


        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        for (i in daysOfWeek.indices) {
            val checkBox = CheckBox(this)
            checkBox.text = daysOfWeek[i]
            checkBox.isChecked = false
            layout.addView(checkBox)
        }

        builder.setView(layout)

        builder.setPositiveButton("OK") { _, _ ->
            val selectedDaysList = mutableListOf<String>()
            for (i in daysOfWeek.indices) {
                val checkBox = layout.getChildAt(i) as CheckBox
                if (checkBox.isChecked) {
                    selectedDaysList.add(daysOfWeek[i])
                }
            }
            scheduleAlarm(hour, minute, selectedDaysList)
        }

        builder.setNegativeButton("Cancel", null)

        builder.show()
    }


    private fun scheduleAlarm(hour: Int, minute: Int, selectedDays: List<String>) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DATE, 1) // Si l'heure est déjà passée, configure l'alarme pour demain
            }
        }

        val alarmTime = calendar.timeInMillis


        val newAlarm = Alarm(
            time = String.format("%02d:%02d", hour, minute),
            days = selectedDays,
            isActive = true
        )


        alarms.add(newAlarm)


        alarmAdapter.notifyItemInserted(alarms.size - 1)


        saveAlarm(hour, minute, selectedDays)


        val workRequest = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            .setInitialDelay(alarmTime - System.currentTimeMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()


        WorkManager.getInstance(this).enqueue(workRequest)
    }


    private fun saveAlarm(hour: Int, minute: Int, selectedDays: List<String>) {
        val editor = sharedPreferences.edit()


        editor.putString("alarm_time", String.format("%02d:%02d", hour, minute))


        editor.putString("alarm_days", selectedDays.joinToString(","))

        editor.apply()
    }

    // SharedPreferences
    private fun loadAlarms() {
        val time = sharedPreferences.getString("alarm_time", null)
        val daysString = sharedPreferences.getString("alarm_days", null)

        if (time != null && daysString != null) {
            val daysList = daysString.split(",")
            val newAlarm = Alarm(time, daysList, true)
            alarms.add(newAlarm)
            alarmAdapter.notifyItemInserted(alarms.size - 1)
        }
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
