package com.example.pecunia.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pecunia.activities.main.MainActivity;
import com.example.pecunia.R;
import com.example.pecunia.data.models.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Registro extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Instancia para la base de datos

    private EditText nombreReg, fechaReg, emailReg, passReg, passConf;
    private RadioGroup rgTipo;
    private Button btnRegistrar;
    private TextView volver, tvInfoPlanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Vincular los nuevos campos del XML
        nombreReg = findViewById(R.id.etNombre);
        fechaReg = findViewById(R.id.etFechaNacimiento);
        emailReg = findViewById(R.id.emailRegistro);
        passReg = findViewById(R.id.passRegistro);
        passConf = findViewById(R.id.passConfirmar);

        rgTipo = findViewById(R.id.rgTipoUsuario);
        tvInfoPlanes = findViewById(R.id.tvInfoPlanes);

        btnRegistrar = findViewById(R.id.btnRegistrarFinal);
        volver = findViewById(R.id.tvVolver);

        // Botón informativo de planes
        tvInfoPlanes.setOnClickListener(v -> mostrarDialogoInfo());

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });

        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void registrarUsuario() {
        final String nombre = nombreReg.getText().toString().trim();
        final String fechaNac = fechaReg.getText().toString().trim();
        final String email = emailReg.getText().toString().trim();
        String pass = passReg.getText().toString().trim();
        String conf = passConf.getText().toString().trim();

        // Capturar el tipo de usuario del RadioGroup
        int selectedId = rgTipo.getCheckedRadioButtonId();
        final String tipoUsuario = (selectedId == R.id.rbPremium) ? "Premium" : "Básico";

        // 1. Validar campos vacíos (ahora incluimos nombre y fecha)
        if (nombre.isEmpty() || fechaNac.isEmpty() || email.isEmpty() || pass.isEmpty() || conf.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validar longitud mínima
        if (pass.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Validar que coincidan
        if (!pass.equals(conf)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Crear usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Si se crea en Auth, procedemos a guardar en Firestore
                            guardarDatosEnFirestore(nombre, email, fechaNac, tipoUsuario);
                        } else {
                            Toast.makeText(Registro.this, "Error al registrar: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void guardarDatosEnFirestore(String nombre, String email, String fechaNacimiento, String tipo) {
        // 1. Obtener el UID del usuario
        String uid = mAuth.getCurrentUser().getUid();

        // 2. CAPTURAR LA FECHA DE REGISTRO AUTOMÁTICAMENTE
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String fechaRegistroAutomatica = format.format(calendar.getTime());

        // 3. Crear el objeto Usuario usando el constructor completo
        // Orden: uid, nombre, email, fechaRegistro, fechaNac, tipo
        Usuario nuevoUsuario = new Usuario(uid, nombre, email, fechaRegistroAutomatica, fechaNacimiento, tipo);

        // 4. Guardar en Firestore
        db.collection("Usuarios").document(uid).set(nuevoUsuario)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Registro.this, "¡Bienvenido a Pecunia!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Registro.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(Registro.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void mostrarDialogoInfo() {
        new AlertDialog.Builder(this)
                .setTitle("Planes Pecunia")
                .setMessage("🔹 Básico: Registro de ingresos y gastos estándar.\n\n👑 Premium: Acceso a categorías, gráficos detallados y gestión avanzada.")
                .setPositiveButton("Entendido", null)
                .show();
    }
}
