package com.example.pecunia.activities.reports;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.pecunia.R;
import com.example.pecunia.activities.forms.FormularioGasto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class GastoClasificado extends AppCompatActivity {

    private TextView tvTitulo;
    private String mes, anio, uid, tipoUsuario;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gasto_clasificado);

        // 1. Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // 2. Recuperar datos del mes y año seleccionados
        mes = getIntent().getStringExtra("MES_SELECCIONADO");
        anio = getIntent().getStringExtra("ANIO_SELECCIONADO");

        // 3. Vincular el título y el botón volver
        tvTitulo = findViewById(R.id.tvTituloClasificado);
        findViewById(R.id.btnVolverClasificado).setOnClickListener(v -> finish());

        // 4. Configurar los clics de todas las tarjetas
        configurarCategorias();

        // 5. Verificar el plan para aplicar estética y bloqueos
        verificarPlan();
    }

    private void configurarCategorias() {
        // Vinculamos cada CardView y le asignamos su categoría correspondiente
        asignarEventoGasto(R.id.cardComida, "Alimentación");
        asignarEventoGasto(R.id.cardSalud, "Salud");
        asignarEventoGasto(R.id.cardOcio, "Ocio");
        asignarEventoGasto(R.id.cardTransporte, "Transporte");
        asignarEventoGasto(R.id.cardCompras, "Compras");
        asignarEventoGasto(R.id.cardHogar, "Hogar");
    }

    private void asignarEventoGasto(int idCard, String nombreCategoria) {
        CardView card = findViewById(idCard);
        card.setOnClickListener(v -> {

            // Si el usuario es básico y elige algo que no sea Alimentación o Salud...
            if ("Básico".equals(tipoUsuario) &&
                    (!nombreCategoria.equals("Alimentación") && !nombreCategoria.equals("Salud"))) {

                Toast.makeText(this, "👑 Categoría bloqueada. ¡Hazte Premium!", Toast.LENGTH_SHORT).show();
            } else {
                // Si tiene permiso, vamos al formulario final
                Intent i = new Intent(GastoClasificado.this, FormularioGasto.class);
                i.putExtra("MES_SELECCIONADO", mes);
                i.putExtra("ANIO_SELECCIONADO", anio);
                i.putExtra("CATEGORIA", nombreCategoria); // Pasamos la categoría seleccionada
                startActivity(i);
            }
        });
    }

    private void verificarPlan() {
        if (uid == null) return;

        db.collection("Usuarios").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                tipoUsuario = documentSnapshot.getString("tipo");

                // Si es Premium, cambiamos el título a Dorado con corona
                if ("Premium".equals(tipoUsuario)) {
                    tvTitulo.setText("CATEGORÍAS PREMIUM 👑");
                    tvTitulo.setTextColor(Color.parseColor("#B8860B")); // Dorado
                }
            }
        });
    }
}
