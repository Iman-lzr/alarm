package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Afficher un Toast ou une notification lorsque l'alarme se déclenche
        Toast.makeText(context, "Alarm triggered!", Toast.LENGTH_SHORT).show()
    }
}
