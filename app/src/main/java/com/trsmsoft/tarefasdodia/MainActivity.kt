package com.trsmsoft.tarefasdodia

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.ArrayAdapter
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskDatabaseHelper: TaskDatabaseHelper
    private val tasks = mutableListOf<Task>()
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 60000L // Atualiza a cada 60 segundos

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

        // Configuração do Spinner
        val spinner = findViewById<Spinner>(R.id.task_priority_spinner)
        if (spinner != null) {
            val adapter = ArrayAdapter.createFromResource(
                this,
                R.array.task_priority_options,
                R.layout.spinner_item
            )
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            spinner.adapter = adapter
        } else {
            Log.e("MainActivity", "Spinner not found!")
        }

        // Carregar e ordenar tarefas
        loadTasks()

        // Configuração do botão de adicionar tarefa
        addButton.setOnClickListener {
            showAddTaskDialog()
        }

        startTaskCheck()
    }


    private fun loadTasks() {
        tasks.clear()
        tasks.addAll(taskDatabaseHelper.getAllTasks())
        // Atualiza o RecyclerView com as tarefas ordenadas
        updateRecyclerViewWithSortedTasks(recyclerView, tasks)
    }

    private fun deleteTask(task: Task) {
        // Cria o diálogo de confirmação
        AlertDialog.Builder(this)
            .setTitle("Confirmação de Exclusão")
            .setMessage("Tem certeza de que deseja excluir a tarefa '${task.name}'?")
            .setPositiveButton("Sim") { _, _ ->
                // Se o usuário confirmar, exclui a tarefa
                taskDatabaseHelper.deleteTask(task.id)
                loadTasks()
            }
            .setNegativeButton("Não", null) // Não faz nada se o usuário cancelar
            .create()
            .show()
    }

    private fun updateTask(task: Task) {
        taskDatabaseHelper.updateTask(task)
    }

//    fun sortTasksByCompletionTime(tasks: List<Task>): List<Task> {
//        return tasks.sortedWith(compareBy<Task> { it.time }
//            .thenBy { it.priority })
//    }

    fun sortTasksByCompletionTime(tasks: List<Task>): List<Task> {
        return tasks.sortedWith(compareBy<Task> { it.priority }
            .thenBy { it.time })
    }

    fun updateRecyclerViewWithSortedTasks(recyclerView: RecyclerView, tasks: List<Task>) {
        val sortedTasks = sortTasksByCompletionTime(tasks)
        val adapter = recyclerView.adapter as? TaskAdapter
        adapter?.updateTasks(sortedTasks)
    }

    private fun showAddTaskDialog() {
        // Infla o layout do diálogo
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.task_name_edit_text)
        val timeEditText = dialogView.findViewById<EditText>(R.id.task_time_edit_text)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.task_priority_spinner)

        // Cria o diálogo
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Adicionar") { _, _ ->
                val name = nameEditText.text.toString()
                val time = timeEditText.text.toString().toIntOrNull() ?: 0
                val priority = prioritySpinner.selectedItemPosition + 1
                val creationTime = System.currentTimeMillis() // Define o timestamp atual

                // Cria a tarefa com o tempo de criação atual
                val task = Task(
                    id = generateTaskId(),
                    name = name,
                    time = time,
                    priority = priority,
                    completed = false,
                    creationTime = creationTime
                )
                taskDatabaseHelper.addTask(task)
                loadTasks()
            }
            .setNegativeButton("Cancelar", null)
            .create()

        // Exibe o diálogo
        dialog.show()

        // Define o fundo do diálogo para ser transparente, deixando o fundo arredondado visível
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    }


    // Função de geração de ID para tarefas
    private fun generateTaskId(): Long {
        // Implementação simples para geração de IDs
        return System.currentTimeMillis()
    }

    private fun startTaskCheck() {
        handler.post(object : Runnable {
            override fun run() {
                checkTasksForCompletion()
                handler.postDelayed(this, updateInterval)
            }
        })
    }

    private fun checkTasksForCompletion() {
        val currentTime = System.currentTimeMillis()
        val tasksToCheck = ArrayList(tasks) // Cria uma cópia da lista de tarefas

        tasksToCheck.forEach { task ->
            val taskEndTime = task.creationTime + (task.time * 60 * 1000)
            if (taskEndTime <= currentTime) {
                // A tarefa está vencida
                if (!task.completed) {
                    task.completed = true
                    taskDatabaseHelper.updateTask(task)
                    loadTasks() // Recarrega as tarefas, o que pode causar uma modificação na lista original
                }
            } else if (taskEndTime - currentTime <= (5 * 60 * 1000)) {
                // Alerta se o tempo restante for menor ou igual a 5 minutos
                // Você pode adicionar um alerta ou uma notificação aqui
                showNotification(this)
            }
        }
    }



    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Channel"
            val channelDescription = "Channel description"

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context) {
        // Criar um canal de notificação (se necessário)
        createNotificationChannel(context)

        // Intent para abrir a atividade quando a notificação for clicada
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Adicionar FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )



        // Criar a notificação
        val notification = NotificationCompat.Builder(context, "my_channel_id")
            .setSmallIcon(R.drawable.ic_notification) // Substitua com seu ícone
            .setContentTitle("Título da Notificação")
            .setContentText("Texto da Notificação")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Mostrar a notificação
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }


}
