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

        // Référence à la FloatingActionButton
        val fab: FloatingActionButton = findViewById(R.id.fab)

        // Création du canal de notification
        createNotificationChannel()

        // Initialiser SharedPreferences
        sharedPreferences = getSharedPreferences("AlarmPreferences", MODE_PRIVATE)

        // Initialiser le RecyclerView
        alarmRecyclerView = findViewById(R.id.recyclerView)
        alarmRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialiser l'adaptateur avec la liste des alarmes
        alarmAdapter = AlarmAdapter(alarms, this)
        alarmRecyclerView.adapter = alarmAdapter

        // Charger les alarmes précédemment enregistrées
        loadAlarms()

        // Clic sur le FloatingActionButton pour afficher le TimePickerDialog
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

    // Méthode pour afficher le dialogue de sélection des jours
    private fun showDaySelectionDialog(hour: Int, minute: Int) {
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val selectedDays = BooleanArray(daysOfWeek.size) // Tableau pour garder la trace des jours sélectionnés

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Days")

        // Créer une vue de cases à cocher pour chaque jour de la semaine
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        for (i in daysOfWeek.indices) {
            val checkBox = CheckBox(this)
            checkBox.text = daysOfWeek[i]
            checkBox.isChecked = false // Initialement, aucun jour n'est sélectionné
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

    // Méthode pour planifier l'alarme
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

        // Créer une nouvelle alarme avec les jours sélectionnés et l'heure
        val newAlarm = Alarm(
            time = String.format("%02d:%02d", hour, minute),
            days = selectedDays, // Utiliser les jours sélectionnés
            isActive = true
        )

        // Ajouter l'alarme à la liste
        alarms.add(newAlarm)

        // Notifier l'adaptateur de la mise à jour
        alarmAdapter.notifyItemInserted(alarms.size - 1)

        // Sauvegarder l'alarme dans SharedPreferences
        saveAlarm(hour, minute, selectedDays)

        // Créer une demande de travail pour la notification de l'alarme
        val workRequest = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            .setInitialDelay(alarmTime - System.currentTimeMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        // Planifier le travail avec WorkManager
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    // Méthode pour enregistrer l'alarme dans SharedPreferences
    private fun saveAlarm(hour: Int, minute: Int, selectedDays: List<String>) {
        val editor = sharedPreferences.edit()

        // Sauvegarder l'heure sous forme de chaîne
        editor.putString("alarm_time", String.format("%02d:%02d", hour, minute))

        // Sauvegarder les jours sélectionnés sous forme de chaîne séparée par des virgules
        editor.putString("alarm_days", selectedDays.joinToString(","))

        editor.apply()
    }

    // Méthode pour charger les alarmes depuis SharedPreferences
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

    // Méthode pour créer le canal de notification (nécessaire pour Android Oreo et versions ultérieures)
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
