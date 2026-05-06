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
import androidx.appcompat.app.AppCompatActivity;

import com.example.pecunia.R;
import com.example.pecunia.activities.forms.FormularioIngreso;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Ingreso extends AppCompatActivity {

    private Button btnAñadir, btnVolver;
    private TextView tvTitulo;
    private String mes, anio, uid;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Recuperamos los datos del Main
        mes = getIntent().getStringExtra("MES_SELECCIONADO");
        anio = getIntent().getStringExtra("ANIO_SELECCIONADO");

        // Vincular vistas
        btnAñadir = findViewById(R.id.btnAbrirFormIngreso);
        btnVolver = findViewById(R.id.btnVolverIngreso);
        tvTitulo = findViewById(R.id.tvTituloIngreso);
        ImageView banner = findViewById(R.id.bannerAnimadoIngreso);

        configurarAnimacion(banner);

        // --- NUEVO: Aplicar estética Premium si corresponde ---
        aplicarEsteticaPremium();

        btnAñadir.setOnClickListener(v -> {
            Intent i = new Intent(this, FormularioIngreso.class);
            i.putExtra("MES_SELECCIONADO", mes);
            i.putExtra("ANIO_SELECCIONADO", anio);
            startActivity(i);
        });

        btnVolver.setOnClickListener(v -> finish());
    }

    private void aplicarEsteticaPremium() {
        db.collection("Usuarios").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String tipo = documentSnapshot.getString("tipo");
                if ("Premium".equals(tipo)) {
                    // Cambios visuales VIP
                    tvTitulo.setText("INGRESOS PREMIUM 👑");
                    tvTitulo.setTextColor(Color.parseColor("#B8860B")); // Dorado
                    btnAñadir.setText("NUEVO INGRESO 👑");
                }
            }
        });
    }

    private void configurarAnimacion(ImageView view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(4000);
        set.start();
    }
}