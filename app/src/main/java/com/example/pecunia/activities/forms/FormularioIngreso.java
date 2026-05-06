package com.example.pecunia.activities.forms;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pecunia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FormularioIngreso extends AppCompatActivity {

    private EditText etDesc, etCant;
    private Button btnGuardar;
    private ImageButton btnVolver;
    private TextView tvTitulo;
    private String mes, anio, uid, tipoUsuario;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_ingreso);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mes = getIntent().getStringExtra("MES_SELECCIONADO");
        anio = getIntent().getStringExtra("ANIO_SELECCIONADO");

        etDesc = findViewById(R.id.etDescripcionIngreso);
        etCant = findViewById(R.id.etCantidadIngreso);
        btnGuardar = findViewById(R.id.btnGuardarIngreso);
        btnVolver = findViewById(R.id.btnVolverAtrasIngreso);
        tvTitulo = findViewById(R.id.tvTituloFormIngreso);

        // --- NUEVO: Verificar Plan para cambiar el botón ---
        verificarPlanUI();

        btnVolver.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarDatos());
    }

    private void verificarPlanUI() {
        db.collection("Usuarios").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tipoUsuario = doc.getString("tipo");
                if ("Premium".equals(tipoUsuario)) {
                    tvTitulo.setText("NUEVO INGRESO 👑");
                    tvTitulo.setTextColor(Color.parseColor("#B8860B"));
                    btnGuardar.setText("GUARDAR 👑");
                    // Cambia el color del botón a dorado
                    btnGuardar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B8860B")));
                }
            }
        });
    }

    private void guardarDatos() {
        String descripcion = etDesc.getText().toString().trim();
        String cantidadStr = etCant.getText().toString().trim();

        if (descripcion.isEmpty() || cantidadStr.isEmpty()) {
            reproducirSonido(R.raw.error);
            Toast.makeText(this, "Rellena los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double cantidad = Double.parseDouble(cantidadStr);

            Map<String, Object> ingreso = new HashMap<>();
            ingreso.put("descripcion", descripcion);
            ingreso.put("cantidad", cantidad);
            ingreso.put("tipo", "ingreso");
            ingreso.put("fechaRegistro", com.google.firebase.Timestamp.now());

            db.collection("Usuarios").document(uid)
                    .collection(anio).document(mes)
                    .collection("Ingresos")
                    .add(ingreso)
                    .addOnSuccessListener(ref -> {
                        reproducirSonido(R.raw.monedas);
                        // Mensaje personalizado VIP
                        String saludoFinal = "Premium".equals(tipoUsuario) ? "¡Ingreso VIP guardado! 👑" : "¡Ingreso guardado!";
                        Toast.makeText(this, saludoFinal, Toast.LENGTH_SHORT).show();
                        finish();
                    });

        } catch (NumberFormatException e) {
            reproducirSonido(R.raw.error);
        }
    }

    private void reproducirSonido(int recurso) {
        MediaPlayer mp = MediaPlayer.create(this, recurso);
        if (mp != null) {
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        }
    }
}
