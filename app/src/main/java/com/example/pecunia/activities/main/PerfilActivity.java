package com.example.pecunia.activities.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pecunia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PerfilActivity extends AppCompatActivity {

    private EditText etNombre, etLimite;
    private RadioGroup rgTipo;
    private RadioButton rbBasico, rbPremium;
    private Button btnVolver, btnGuardar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String planActualBD = "Básico";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogo_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNombre = findViewById(R.id.etPerfilNombre);
        etLimite = findViewById(R.id.etPerfilLimite);
        rgTipo = findViewById(R.id.rgPerfilTipo);
        rbBasico = findViewById(R.id.rbPerfilBasico);
        rbPremium = findViewById(R.id.rbPerfilPremium);
        btnVolver = findViewById(R.id.btnPerfilVolver);
        btnGuardar = findViewById(R.id.btnPerfilGuardar);

        cargarDatosFirebase();

        // Botón Volver: Cierra la actividad actual y regresa al menú de inmediato sin guardar nada
        btnVolver.setOnClickListener(v -> finish());

        // Botón Guardar: Primero valida los campos y luego pide confirmación antes de modificar Firebase
        btnGuardar.setOnClickListener(v -> procesarGuardadoConConfirmacion());
    }

    private void cargarDatosFirebase() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etNombre.setText(documentSnapshot.getString("nombre"));

                        // Si el usuario ya tiene un límite personalizado en la BD lo cargamos, si no, ponemos 50.0 por defecto
                        if (documentSnapshot.contains("limiteAviso")) {
                            etLimite.setText(String.valueOf(documentSnapshot.getDouble("limiteAviso")));
                        } else {
                            etLimite.setText("50.0");
                        }

                        planActualBD = documentSnapshot.getString("tipo");
                        if ("Premium".equals(planActualBD)) {
                            rbPremium.setChecked(true);
                        } else {
                            rbBasico.setChecked(true);
                        }
                    }
                });
    }

    private void procesarGuardadoConConfirmacion() {
        String nuevoNombre = etNombre.getText().toString().trim();
        String nuevoLimiteStr = etLimite.getText().toString().trim();

        if (nuevoNombre.isEmpty() || nuevoLimiteStr.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double nuevoLimite = Double.parseDouble(nuevoLimiteStr);
        String planSeleccionado = (rgTipo.getCheckedRadioButtonId() == R.id.rbPerfilPremium) ? "Premium" : "Básico";

        // Aquí está el cuadro de diálogo que confirma los cambios antes de salir de la pantalla
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Modificaciones")
                .setMessage("¿Está seguro de que desea guardar los cambios en su perfil y salir?")
                .setPositiveButton("Sí, guardar", (dialog, which) -> {
                    // Si dice que sí, llamamos al método que actualiza Firebase de verdad
                    subirDatosAFirebase(nuevoNombre, nuevoLimite, planSeleccionado);
                })
                .setNegativeButton("Revisar", (dialog, which) -> dialog.dismiss()) // Si dice que no, se cierra el aviso y no pasa nada
                .show();
    }

    private void subirDatosAFirebase(String nombre, double limite, String plan) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> actualizaciones = new HashMap<>();
        actualizaciones.put("nombre", nombre);
        actualizaciones.put("limiteAviso", limite);
        actualizaciones.put("tipo", plan);

        // Actualizamos los datos en caliente en la colección de Firestore
        db.collection("Usuarios").document(uid).update(actualizaciones)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PerfilActivity.this, "Datos guardados en la nube", Toast.LENGTH_SHORT).show();
                    finish(); // Destruye esta pantalla y vuelve automáticamente al MainActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PerfilActivity.this, "Error al sincronizar con el servidor", Toast.LENGTH_SHORT).show();
                });
    }
}
