package com.trsmsoft.tarefasdodia

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskDatabaseHelper: TaskDatabaseHelper
    private val tasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        recyclerView = findViewById(R.id.recycler_view)
        val addButton: Button = findViewById(R.id.add_task_button)


        // Configuração do RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(
            tasks,
            { task -> deleteTask(task) },
            { task -> updateTask(task) }
        )
        recyclerView.adapter = taskAdapter

        taskDatabaseHelper = TaskDatabaseHelper(this)

        loadTasks()

        // Configuração do botão de adicionar tarefa
        addButton.setOnClickListener {
            showAddTaskDialog()
        }
        // Adicionar a linha separadora personalizada
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider)!!)
        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun loadTasks() {
        tasks.clear()
        tasks.addAll(taskDatabaseHelper.getAllTasks())
        taskAdapter.notifyDataSetChanged()
    }

    private fun deleteTask(task: Task) {
        taskDatabaseHelper.deleteTask(task.id)
        loadTasks()
    }

    private fun updateTask(task: Task) {
        taskDatabaseHelper.updateTask(task)
    }

    private fun showAddTaskDialog() {
        // Cria um novo diálogo para adicionar uma tarefa
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.task_name_edit_text)
        val timeEditText = dialogView.findViewById<EditText>(R.id.task_time_edit_text)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.task_priority_spinner)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEditText.text.toString()
                val time = timeEditText.text.toString().toIntOrNull() ?: 0
                val priority = prioritySpinner.selectedItemPosition + 1
                val task = Task(name = name, time = time, priority = priority, completed = false)
                taskDatabaseHelper.addTask(task)
                loadTasks()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}
