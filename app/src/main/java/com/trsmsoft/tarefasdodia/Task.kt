package com.trsmsoft.tarefasdodia

data class Task(
    val id: Long = 0,
    val name: String,
    val time: Int,
    val priority: Int,
    var completed: Boolean
)
