package com.trsmsoft.tarefasdodia

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_PRIORITY = "priority"
        private const val COLUMN_COMPLETED = "completed"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, "
                + "$COLUMN_TIME INTEGER, "
                + "$COLUMN_PRIORITY INTEGER, "
                + "$COLUMN_COMPLETED INTEGER)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Métodos adicionais para inserir, atualizar e buscar tarefas

    fun addTask(task: Task): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, task.name)
            put(COLUMN_TIME, task.time)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_COMPLETED, if (task.completed) 1 else 0)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun getAllTasks(): List<Task> {
        val db = readableDatabase
        val tasks = mutableListOf<Task>()
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_ID))
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val time = getInt(getColumnIndexOrThrow(COLUMN_TIME))
                val priority = getInt(getColumnIndexOrThrow(COLUMN_PRIORITY))
                val completed = getInt(getColumnIndexOrThrow(COLUMN_COMPLETED)) == 1
                tasks.add(Task(id, name, time, priority, completed))
            }
        }
        cursor.close()
        return tasks
    }

    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, task.name)
            put(COLUMN_TIME, task.time)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_COMPLETED, if (task.completed) 1 else 0)
        }
        return db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
    }

    fun deleteTask(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

}
