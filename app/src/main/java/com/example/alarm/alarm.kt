package com.example.alarm

data class Alarm(
    val time: String,
    val days: List<String>,
    var isActive: Boolean = false // Nouveau champ pour savoir si l'alarme est activée
)
