package com.example.pecunia.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pecunia.R;
import com.example.pecunia.activities.reports.Resumen;
import com.example.pecunia.activities.reports.ResumenBasico;
import com.example.pecunia.activities.auth.Inicio;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerAnio, spinnerMes;
    private BottomNavigationView bottomNav;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String nombreUsuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        spinnerAnio = findViewById(R.id.spinnerAnio);
        spinnerMes = findViewById(R.id.spinnerMes);
        bottomNav = findViewById(R.id.bottom_navigation);

        obtenerDatosUsuario();
        configurarSpinners();

        // Recuperar fecha si volvemos de un formulario
        procesarIntent(getIntent());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ingresos) {
                abrirPantalla(Ingreso.class);
                return true;
            } else if (id == R.id.nav_gastos) {
                abrirPantalla(Gasto.class);
                return true;
            } else if (id == R.id.nav_resumen) {
                lanzarResumenSegunPlan();
                return true;
            } else if (id == R.id.nav_perfil) {
                // NUEVO: Abre la actividad de perfil en pantalla completa
                Intent intent = new Intent(MainActivity.this, PerfilActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_logout) {
                cerrarSesionYSaltar();
                return true;
            }
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cerrarSesionYSaltar();
            }
        });
    }

    // NUEVO: Al volver de PerfilActivity, este método refresca la cabecera automáticamente
    @Override
    protected void onResume() {
        super.onResume();
        obtenerDatosUsuario();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Actualiza el intent de la actividad
        procesarIntent(intent);
    }

    private void procesarIntent(Intent intent) {
        if (intent != null && intent.hasExtra("MES_SELECCIONADO")) {
            String mesExtra = intent.getStringExtra("MES_SELECCIONADO");
            String anioExtra = intent.getStringExtra("ANIO_SELECCIONADO");

            // Buscamos la posición en los arrays para poner el Spinner en su sitio
            actualizarSeleccionSpinners(mesExtra, anioExtra);
        }
    }

    private void actualizarSeleccionSpinners(String mes, String anio) {
        // Para el Mes
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        int posMes = Arrays.asList(meses).indexOf(mes);
        if (posMes >= 0) spinnerMes.setSelection(posMes);

        // Para el Año
        String[] anios = {"2024", "2025", "2026", "2027"};
        int posAnio = Arrays.asList(anios).indexOf(anio);
        if (posAnio >= 0) spinnerAnio.setSelection(posAnio);
    }

    private void obtenerDatosUsuario() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            db.collection("Usuarios").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            nombreUsuario = documentSnapshot.getString("nombre");
                            String tipoPlan = documentSnapshot.getString("tipo");

                            TextView tvSaludo = findViewById(R.id.tvSaludo);

                            if ("Premium".equals(tipoPlan)) {
                                if (tvSaludo != null) {
                                    tvSaludo.setText("👑 Hola, " + nombreUsuario);
                                    tvSaludo.setTextColor(android.graphics.Color.parseColor("#B8860B"));
                                }
                            } else {
                                if (tvSaludo != null) {
                                    tvSaludo.setText("Hola, " + nombreUsuario);
                                    tvSaludo.setTextColor(android.graphics.Color.parseColor("#1B5E20"));
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void configurarSpinners() {
        String[] anios = {"2024", "2025", "2026", "2027"};
        spinnerAnio.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, anios));
        spinnerAnio.setSelection(2); // 2026 por defecto

        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        spinnerMes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, meses));
    }

    private void lanzarResumenSegunPlan() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        String mes = spinnerMes.getSelectedItem().toString();
        String anio = spinnerAnio.getSelectedItem().toString();

        db.collection("Usuarios").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String tipoPlan = documentSnapshot.getString("tipo");
                Intent intent;

                if ("Premium".equals(tipoPlan)) {
                    intent = new Intent(MainActivity.this, Resumen.class);
                } else {
                    intent = new Intent(MainActivity.this, ResumenBasico.class);
                }

                intent.putExtra("MES_SELECCIONADO", mes);
                intent.putExtra("ANIO_SELECCIONADO", anio);
                startActivity(intent);
            }
        });
    }

    private void abrirPantalla(Class<?> destino) {
        String mes = spinnerMes.getSelectedItem().toString();
        String anio = spinnerAnio.getSelectedItem().toString();

        Intent intent = new Intent(MainActivity.this, destino);
        intent.putExtra("MES_SELECCIONADO", mes);
        intent.putExtra("ANIO_SELECCIONADO", anio);
        startActivity(intent);
    }

    private void cerrarSesionYSaltar() {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, Inicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}