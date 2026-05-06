package com.example.pecunia.activities.main;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pecunia.activities.reports.GastoClasificado;
import com.example.pecunia.R;
import com.example.pecunia.data.network.RetiradaEfectivo;
import com.example.pecunia.activities.forms.FormularioGasto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Gasto extends AppCompatActivity {

    private Button btnTarjeta, btnEfectivo, btnVolver;
    private TextView tvTitulo;
    private String mes, anio, uid;
    private String tipoUsuario = "Básico"; // Por defecto lo tratamos como básico hasta que Firebase diga lo contrario
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gasto);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        mes = getIntent().getStringExtra("MES_SELECCIONADO");
        anio = getIntent().getStringExtra("ANIO_SELECCIONADO");

        tvTitulo = findViewById(R.id.tvTituloGasto);
        btnTarjeta = findViewById(R.id.btnGastoTarjeta);
        btnEfectivo = findViewById(R.id.btnRetiradaEfectivo);
        btnVolver = findViewById(R.id.btnVolverGasto);
        ImageView banner = findViewById(R.id.bannerAnimadoGasto);

        configurarAnimacion(banner);
        verificarPlanUsuario();

        // MODIFICACIÓN: Al pulsar Gasto Tarjeta
        btnTarjeta.setOnClickListener(v -> {
            if ("Premium".equals(tipoUsuario)) {
                // Si es Premium, va a elegir el icono (Ocio, Salud, etc.)
                Intent i = new Intent(this, GastoClasificado.class);
                i.putExtra("MES_SELECCIONADO", mes);
                i.putExtra("ANIO_SELECCIONADO", anio);
                startActivity(i);
            } else {
                // Si es Básico, SALTA DIRECTAMENTE al formulario con categoría "General"
                Intent i = new Intent(this, FormularioGasto.class);
                i.putExtra("MES_SELECCIONADO", mes);
                i.putExtra("ANIO_SELECCIONADO", anio);
                i.putExtra("CATEGORIA", "General"); // El básico no clasifica
                startActivity(i);
            }
        });

        // Para el efectivo puedes hacer lo mismo o mandarlo a su propia clase si la tienes
        btnEfectivo.setOnClickListener(v -> {
                    if ("Premium".equals(tipoUsuario)) {
                        // PREMIUM: Accede a los cajeros
                        Intent i = new Intent(this, RetiradaEfectivo.class);
                        i.putExtra("MES_SELECCIONADO", mes);
                        i.putExtra("ANIO_SELECCIONADO", anio);
                        startActivity(i);
                    } else {
                        // BÁSICO: Bloqueo total con aviso
                        Toast.makeText(this, "👑 Esta función es exclusiva para Usuarios Premium", Toast.LENGTH_LONG).show();
                    }
                });

        btnVolver.setOnClickListener(v -> finish());
    }

    private void verificarPlanUsuario() {
        if (uid == null) return;

        db.collection("Usuarios").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                tipoUsuario = documentSnapshot.getString("tipo");

                if ("Premium".equals(tipoUsuario)) {
                    tvTitulo.setText("GESTIÓN DE GASTOS 👑");
                    tvTitulo.setTextColor(Color.parseColor("#B8860B"));
                    btnTarjeta.setText("GASTO TARJETA 👑");
                    btnEfectivo.setText("RETIRADA CAJERO 👑");
                }
            }
        });
    }

    private void configurarAnimacion(ImageView view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0.7f, 1f);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        alpha.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatMode(ValueAnimator.REVERSE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(4000);
        set.start();
    }
}