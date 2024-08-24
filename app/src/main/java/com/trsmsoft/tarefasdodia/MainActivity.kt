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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.os.CountDownTimer
import android.util.Log
import android.widget.ArrayAdapter


class MainActivity : AppCompatActivity() {
    private lateinit var notificationHelper: NotificationHelper
    private var countDownTimer: CountDownTimer? = null



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

        notificationHelper = NotificationHelper(this)



        // Configuração do RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(
            tasks,
            { task -> deleteTask(task) },
            { task -> updateTask(task) },
            this
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

    // Define os métodos updateTaskTimes e startTaskCheck aqui

    private fun updateTaskTimes() {
        tasks.forEach { task ->
            // Atualiza o tempo restante na tarefa
            taskDatabaseHelper.updateTask(task)
        }
        // Notifica o adaptador sobre as mudanças
        updateRecyclerViewWithSortedTasks(recyclerView, tasks)
    }

    private fun startTaskCheck() {
        handler.post(object : Runnable {
            override fun run() {
                checkTasksForCompletion()
                updateTaskTimes() // Atualiza o tempo restante das tarefas
                handler.postDelayed(this, updateInterval)
            }
        })
    }

//    private fun calculateTotalTimeInMillis(hours: Int, minutes: Int): Long {
//        val totalMinutes = hours * 60 + minutes
//        return totalMinutes * 60 * 1000L // Convert minutes to milliseconds
//    }


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

    private fun sortTasksByCompletionTime(tasks: List<Task>): List<Task> {
        return tasks.sortedWith(compareBy<Task> { it.priority }
            .thenBy { it.time })
    }

    private fun updateRecyclerViewWithSortedTasks(recyclerView: RecyclerView, tasks: List<Task>) {
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
                    halfTimePassed = false,
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

//    private fun startTaskCheck() {
//        handler.post(object : Runnable {
//            override fun run() {
//                checkTasksForCompletion()
//                handler.postDelayed(this, updateInterval)
//            }
//        })
//    }


    private fun checkTasksForCompletion() {
        val currentTime = System.currentTimeMillis()
        val tasksToCheck = ArrayList(tasks) // Cria uma cópia da lista de tarefas

        tasksToCheck.forEach { task ->
            val taskEndTime = task.creationTime + (task.time * 60 * 1000)
            val halfTime = task.creationTime + (task.time * 30 * 1000) // Metade do tempo da tarefa em milissegundos

            when {
                taskEndTime <= currentTime -> {
                    // A tarefa está vencida
                    if (!task.completed) {
                        task.completed = true
                        taskDatabaseHelper.updateTask(task)
                        notificationHelper.sendOverTimeNotification(task.id, task.name)
                        loadTasks()
                    }
                }
                currentTime >= halfTime && !task.halfTimePassed -> {
                    // O tempo restante é menor que metade do tempo original e ainda não foi notificado
                    task.halfTimePassed = true
                    taskDatabaseHelper.updateTask(task)
                    notificationHelper.sendHalfTimeNotification(task.id, task.name)

                }
            }
        }

        // Atualiza a lista de tarefas na interface do usuário
        loadTasks()
    }


//    private fun createNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channelId = "my_channel_id"
//            val channelName = "My Channel"
//            val channelDescription = "Channel description"
//
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(channelId, channelName, importance).apply {
//                description = channelDescription
//            }
//
//            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }

//    fun showNotification(context: Context, taskName: String) {
//        // Criar um canal de notificação (se necessário)
//        createNotificationChannel(context)
//
//        // Intent para abrir a atividade quando a notificação for clicada
//        val intent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//
//        // Adicionar FLAG_IMMUTABLE
//        val pendingIntent = PendingIntent.getActivity(
//            context,
//            0,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // Configurar o som da notificação
//        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm}")
//
//
//        // Criar a notificação
//        val notification = NotificationCompat.Builder(context, "my_channel_id")
//            .setSmallIcon(R.drawable.ic_notification) // Substitua com seu ícone
//            .setContentTitle("Tarefa: $taskName") // Usa o nome da tarefa
//            .setContentText("Você tem uma tarefa pendente: $taskName")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setSound(soundUri)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .build()
//
//        // Mostrar a notificação
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(1, notification)
//    }


//    private fun startTimer(totalTimeInMillis: Long) {
//        val halfTime = totalTimeInMillis / 2
//        val ninetyPercentTime = totalTimeInMillis * 0.9
//
//        countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                val elapsedTime = totalTimeInMillis - millisUntilFinished
//
//                if (elapsedTime >= halfTime && elapsedTime < halfTime + 1000) {
//                    showHalfTimeAlert()
//                }
//
//                if (elapsedTime >= ninetyPercentTime && elapsedTime < ninetyPercentTime + 1000) {
//                    showNinetyPercentAlert()
//                }
//            }
//
//            override fun onFinish() {
//                showEndTimeAlert()
//            }
//        }.start()
//    }

//    private fun showHalfTimeAlert() {
//        runOnUiThread {
//            if (!isFinishing) {
//                AlertDialog.Builder(this)
//                    .setTitle("Metade do Tempo!")
//                    .setMessage("Você chegou na metade do tempo disponível.")
//                    .setPositiveButton("OK", null)
//                    .show()
//            }
//        }
//    }

//    private fun showNinetyPercentAlert() {
//        runOnUiThread {
//            if (!isFinishing) {
//                AlertDialog.Builder(this)
//                    .setTitle("90% do Tempo Passado!")
//                    .setMessage("Você já usou 90% do tempo disponível.")
//                    .setPositiveButton("OK", null)
//                    .show()
//            }
//        }
//    }

//    private fun showEndTimeAlert() {
//        runOnUiThread {
//            if (!isFinishing) {
//                AlertDialog.Builder(this)
//                    .setTitle("Tempo Acabado!")
//                    .setMessage("O tempo disponível acabou.")
//                    .setPositiveButton("OK") { _, _ -> finish() }
//                    .show()
//            }
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

