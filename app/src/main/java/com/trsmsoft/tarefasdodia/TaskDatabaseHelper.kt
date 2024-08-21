package com.trsmsoft.tarefasdodia

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 2 // Incrementar a versão do banco de dados
        private const val TABLE_NAME = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_PRIORITY = "priority"
        private const val COLUMN_COMPLETED = "completed"
        private const val COLUMN_CREATION_TIME = "created_at"
        private const val COLUMN_HALF_TIME_PASSED = "halfTimePassed" // Nova coluna
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_TIME INTEGER,
                $COLUMN_PRIORITY INTEGER,
                $COLUMN_COMPLETED INTEGER,
                $COLUMN_CREATION_TIME INTEGER,
                $COLUMN_HALF_TIME_PASSED INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_HALF_TIME_PASSED INTEGER DEFAULT 0")
        }
    }

    fun addTask(task: Task): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, task.name)
            put(COLUMN_TIME, task.time)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_COMPLETED, if (task.completed) 1 else 0)
            put(COLUMN_CREATION_TIME, task.creationTime)
            put(COLUMN_HALF_TIME_PASSED, if (task.halfTimePassed) 1 else 0) // Adiciona valor para a nova coluna

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
                val halfTimePassed = getInt(getColumnIndexOrThrow(COLUMN_HALF_TIME_PASSED)) > 0
                val creationTime = getLong(getColumnIndexOrThrow(COLUMN_CREATION_TIME)) // Adiciona a leitura da coluna `created_at`
                tasks.add(Task(id, name, time, priority, completed, halfTimePassed, creationTime))
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
            put(COLUMN_CREATION_TIME, task.creationTime)
            put(COLUMN_HALF_TIME_PASSED, if (task.halfTimePassed) 1 else 0) // Atualiza valor para a nova coluna
        }
        return db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
    }

    fun deleteTask(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}


