package com.example.pecunia.activities.auth;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pecunia.activities.main.MainActivity;
import com.example.pecunia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class Entrar extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText usu, cont;
    private Button aceptar;
    private TextView volver;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrar);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        usu = findViewById(R.id.usuarioR);
        cont = findViewById(R.id.contraseñaR);
        aceptar = findViewById(R.id.btnAceptarR);
        volver = findViewById(R.id.tvVolver);

        aceptar.setOnClickListener(v -> loginUser());
        volver.setOnClickListener(v -> finish());
    }

    private void loginUser() {
        String email = usu.getText().toString().trim();
        String password = cont.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Entrar.this, "Debe rellenar todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // --- GUARDAR EL TOKEN EN FIRESTORE ---
                        String userId = mAuth.getCurrentUser().getUid();
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(taskToken -> {
                                    if (taskToken.isSuccessful()) {
                                        String token = taskToken.getResult();
                                        // Cambia "Usuarios" por el nombre exacto de tu colección en Firestore
                                        FirebaseFirestore.getInstance().collection("Usuarios")
                                                .document(userId)
                                                .update("fcmToken", token)
                                                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token guardado"))
                                                .addOnFailureListener(e -> Log.e("FCM", "Error al guardar token", e));
                                    }
                                });

                        Toast.makeText(Entrar.this, "¡Bienvenido a Pecunia!", Toast.LENGTH_SHORT).show();
                        mp = MediaPlayer.create(Entrar.this, R.raw.bienvenida);
                        mp.start();

                        startActivity(intentMain());
                        finish();
                    } else {
                        Toast.makeText(Entrar.this, "Error de credenciales.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private Intent intentMain() {
        return new Intent(Entrar.this, MainActivity.class);
    }
}
