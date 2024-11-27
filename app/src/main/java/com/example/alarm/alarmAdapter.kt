package com.example.alarm

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmAdapter(private val alarms: MutableList<Alarm>, private val context: Context) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.timeText)
        val daysText: TextView = itemView.findViewById(R.id.daysText)
        val alarmSwitch: Switch = itemView.findViewById(R.id.toggleSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.timeText.text = alarm.time
        holder.daysText.text = alarm.days.joinToString(", ") // Affiche les jours
        holder.alarmSwitch.isChecked = alarm.isActive // Met à jour l'état du Switch selon l'alarme

        // Ajouter un listener pour l'état du Switch
        holder.alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarm.isActive = isChecked
            if (isChecked) {
                sendNotification(alarm)
            }
        }
    }

    override fun getItemCount(): Int = alarms.size

    @SuppressLint("MissingPermission")
    private fun sendNotification(alarm: Alarm) {
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setContentTitle("Alarm Set!")
            .setContentText("Your alarm is set for ${alarm.time}.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, notification)
    }
}
