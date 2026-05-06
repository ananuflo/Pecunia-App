package com.example.pecunia.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper; // Importante añadir esta
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pecunia.R;

public class Splash extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 1. Buscamos el ImageView que creamos en el XML
        ImageView ivGif = findViewById(R.id.iv_gif_splash);

        // 2. Usamos Glide para cargar el GIF
        Glide.with(this)
                .asGif()
                .load(R.drawable.cerdito_splash) // Nombre de tu archivo en drawable
                .into(ivGif);

        // 3. Tu código de salto de pantalla (esperar 3.5 segundos)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Splash.this, Inicio.class);
                startActivity(i);
                finish();
            }
        }, 4500);
    }
}