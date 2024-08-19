package com.trsmsoft.tarefasdodia

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class TaskAdapter(
    private val tasks: List<Task>,
    private val onDeleteClick: (Task) -> Unit,
    private val onTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.task_name)
        val timeTextView: TextView = itemView.findViewById(R.id.task_time_value)
        val priorityTextView: TextView = itemView.findViewById(R.id.task_priority_value)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.nameTextView.text = task.name
        holder.timeTextView.text = "${task.time} min"
        holder.priorityTextView.text = when (task.priority) {
            1 -> {
                holder.priorityTextView.setTextColor(Color.RED)
                "Alta"
            }
            2 -> {
                holder.priorityTextView.setTextColor(Color.YELLOW)
                "Média"
            }
            3 -> {
                holder.priorityTextView.setTextColor(Color.GREEN)
                "Baixa"
            }
            else -> {
                holder.priorityTextView.setTextColor(Color.GRAY)
                "Desconhecida"
            }
        }

        holder.deleteButton.setOnClickListener { onDeleteClick(task) }
    }

    override fun getItemCount(): Int = tasks.size
}