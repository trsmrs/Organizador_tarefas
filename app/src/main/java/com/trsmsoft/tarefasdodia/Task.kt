package com.trsmsoft.tarefasdodia

data class Task(
    val id: Long = 0,
    val name: String,
    val time: Int,
    val priority: Int,
    var completed: Boolean,
    var halfTimePassed: Boolean,
val creationTime: Long // Adicionado
) {
    constructor(name: String, time: Int, priority: Int, completed: Boolean, halfTimePassed: Boolean, creationTime: Long) :
    this(0, name, time, priority, completed, halfTimePassed, creationTime) // Adicionado id padrão
}