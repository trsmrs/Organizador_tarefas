package com.trsmsoft.tarefasdodia

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class TaskAdapter(
    private var tasks: List<Task>,
    private val onDeleteClick: (Task) -> Unit,
    private val onTaskClick: (Task) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val notificationHelper = NotificationHelper(context)

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.task_name)
        val timeTextView: TextView = itemView.findViewById(R.id.task_time_value)
        val priorityTextView: TextView = itemView.findViewById(R.id.task_priority_value)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)

        fun bind(task: Task) {
            // Configura o nome da tarefa
            nameTextView.text = task.name

            // Configura o tempo restante da tarefa
            timeTextView.text = "${task.getRemainingTime()} min"

            // Configura a prioridade da tarefa e a cor correspondente
            priorityTextView.text = when (task.priority) {
                1 -> {
                    priorityTextView.setTextColor(Color.RED)
                    "Alta"
                }
                2 -> {
                    priorityTextView.setTextColor(Color.YELLOW)
                    "Média"
                }
                3 -> {
                    priorityTextView.setTextColor(Color.GREEN)
                    "Baixa"
                }
                else -> {
                    priorityTextView.setTextColor(Color.GRAY)
                    "Desconhecida"
                }
            }

            // Configura o botão de exclusão
            deleteButton.setOnClickListener { onDeleteClick(task) }

            // Lógica para determinar o fundo baseado no tempo restante
            val currentTime = System.currentTimeMillis()
            val taskEndTime = task.creationTime + (task.time * 60 * 1000L)
            val halfTime = task.creationTime + (task.time * 30 * 1000L)

            when {
                taskEndTime <= currentTime -> {
                    itemView.setBackgroundResource(R.drawable.rounded_background_overdue) // Fundo vermelho se a tarefa estiver vencida
                }
                currentTime >= halfTime -> {
                    itemView.setBackgroundResource(R.drawable.rounded_background_half_time) // Fundo amarelo se o tempo restante for menor que metade do tempo original
                }
                else -> {
                    itemView.setBackgroundResource(R.drawable.rounded_background) // Fundo padrão se a tarefa não estiver atrasada e o tempo restante não for menor que a metade
                }
            }

            // Configura o clique no item
            itemView.setOnClickListener {
                onTaskClick(task)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)

        val currentTime = System.currentTimeMillis()
        val halfTime = task.creationTime + (task.time * 30 * 1000L)

        if (!task.halfTimePassed && currentTime >= halfTime) {
            task.halfTimePassed = true
            notificationHelper.sendHalfTimeNotification(task.id, task.name)

        }

        holder.nameTextView.text = task.name
        holder.timeTextView.text = "${task.getRemainingTime()} min"
    }

    override fun getItemCount(): Int = tasks.size
}
