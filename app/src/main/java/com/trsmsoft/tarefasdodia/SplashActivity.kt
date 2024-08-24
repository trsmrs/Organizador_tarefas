package com.trsmsoft.tarefasdodia

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Define um atraso para a splash screen antes de iniciar a MainActivity
        Handler().postDelayed({
            // Inicia a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            // Fecha a SplashActivity para que o usuário não possa voltar para ela
            finish()
        }, 2000) // Tempo de exibição da splash screen em milissegundos (ex: 2000ms = 2 segundos)
    }
}
