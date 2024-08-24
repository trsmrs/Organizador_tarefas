package com.trsmsoft.tarefasdodia

data class Task(
    val id: Long = 0,
    val name: String,
    var time: Int, // Tempo em minutos
    val priority: Int,
    var completed: Boolean,
    var halfTimePassed: Boolean,
    val creationTime: Long // Tempo de criação da tarefa
) {
    constructor(name: String, time: Int, priority: Int, completed: Boolean, halfTimePassed: Boolean, creationTime: Long) :
            this(0, name, time, priority, completed, halfTimePassed, creationTime)

    // Calcula o tempo restante em minutos
    fun getRemainingTime(): Long {
        val endTime = creationTime + (time * 60 * 1000L)
        val currentTime = System.currentTimeMillis()
        return ((endTime - currentTime + 59999) / 60000).coerceAtLeast(0)
    }
}


