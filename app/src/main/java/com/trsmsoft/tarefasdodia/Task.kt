package com.trsmsoft.tarefasdodia

data class Task(
    val id: Long = 0,
    val name: String,
    val time: Int,
    val priority: Int,
    var completed: Boolean,
val creationTime: Long // Adicionado
) {
    constructor(name: String, time: Int, priority: Int, completed: Boolean, creationTime: Long) :
    this(0, name, time, priority, completed, creationTime) // Adicionado id padrão
}