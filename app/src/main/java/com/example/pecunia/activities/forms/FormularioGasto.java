package com.example.pecunia.activities.forms;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pecunia.R;
import com.example.pecunia.activities.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FormularioGasto extends AppCompatActivity {

    private EditText etDesc, etCant;
    private Button btnGuardar;
    private TextView tvTitulo;
    private String mes, anio, categoria, uid;
    private FirebaseFirestore db;
    private String tipoUsuario = "Básico"; // Por defecto hasta consultar Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_gasto);

        // 1. Inicializar Firebase y Datos del Intent
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        mes = getIntent().getStringExtra("MES_SELECCIONADO");
        anio = getIntent().getStringExtra("ANIO_SELECCIONADO");
        categoria = getIntent().getStringExtra("CATEGORIA");

        // 2. Vincular Vistas
        etDesc = findViewById(R.id.etDescripcionGasto);
        etCant = findViewById(R.id.etCantidadGasto);
        btnGuardar = findViewById(R.id.btnGuardarGastoFinal);
        tvTitulo = findViewById(R.id.tvTituloFormGasto);

        // 3. Configurar Interfaz según Plan (Premium/Básico)
        verificarPlanYConfigurar();

        // 4. Listeners
        findViewById(R.id.btnVolverFormGasto).setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarEnFirebase());
    }

    private void verificarPlanYConfigurar() {
        if (uid == null) return;

        db.collection("Usuarios").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tipoUsuario = doc.getString("tipo");

                if ("Premium".equals(tipoUsuario)) {
                    // --- ESTÉTICA PREMIUM 👑 ---
                    if (categoria != null) {
                        tvTitulo.setText("GASTO: " + categoria.toUpperCase() + " 👑");
                    }
                    tvTitulo.setTextColor(Color.parseColor("#B8860B")); // Dorado
                    btnGuardar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B8860B")));
                    btnGuardar.setText("GUARDAR GASTO PREMIUM 👑");
                } else {
                    // --- ESTÉTICA BÁSICA ---
                    // En plan básico, aunque venga una categoría del intent, la forzamos a "General"
                    categoria = "General";
                    tvTitulo.setText("REGISTRAR GASTO");
                    tvTitulo.setTextColor(Color.BLACK);
                    btnGuardar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1B5E20"))); // Verde
                }
            }
        });
    }

    private void guardarEnFirebase() {
        String descripcion = etDesc.getText().toString().trim();
        String cantidadStr = etCant.getText().toString().trim();

        // Validación de campos vacíos
        if (descripcion.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double cantidad = Double.parseDouble(cantidadStr);

        // CAPTURA AUTOMÁTICA DE FECHA (Formato DD/MM/AAAA)
        String fechaHoy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // Crear el mapa de datos para Firebase
        Map<String, Object> gasto = new HashMap<>();
        gasto.put("descripcion", descripcion);
        gasto.put("cantidad", cantidad);
        gasto.put("fecha", fechaHoy); // <-- Clave para tu tabla de informes
        gasto.put("tipo", "gasto");

        // Seguridad: Si por algún motivo categoria es null, asignamos Otros
        if (categoria == null) categoria = "Otros";

        // Asignar categoría según el plan validado
        if ("Premium".equals(tipoUsuario)) {
            gasto.put("categoria", categoria);
        } else {
            gasto.put("categoria", "General");
        }

        // Subir a la colección jerárquica: Usuarios > UID > Año > Mes > Gastos
        db.collection("Usuarios").document(uid)
                .collection(anio).document(mes)
                .collection("Gastos")
                .add(gasto)
                .addOnSuccessListener(documentReference -> {
                    reproducirSonido();
                    Toast.makeText(this, "¡Gasto registrado con éxito!", Toast.LENGTH_SHORT).show();

                    // --- MEJORA DE NAVEGACIÓN: Volver al Main manteniendo el contexto ---
                    Intent intent = new Intent(this, MainActivity.class);

                    // Pasamos de vuelta el mes y el año para evitar que el Main se resetee a Enero
                    intent.putExtra("MES_SELECCIONADO", mes);
                    intent.putExtra("ANIO_SELECCIONADO", anio);

                    // FLAG_ACTIVITY_CLEAR_TOP cierra las actividades intermedias
                    // FLAG_ACTIVITY_SINGLE_TOP reutiliza la instancia existente del MainActivity
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show();
                });
    }

    private void reproducirSonido() {
        try {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.gasto);
            if (mp != null) {
                mp.setOnCompletionListener(MediaPlayer::release); // Importante para no saturar la memoria
                mp.start();
            }
        } catch (Exception e) {
            e.printStackTrace(); // Evita que la app se cierre si falta el archivo de sonido
        }
    }
}
