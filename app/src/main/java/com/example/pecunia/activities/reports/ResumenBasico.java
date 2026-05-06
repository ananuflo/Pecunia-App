package com.example.pecunia.activities.reports;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.pecunia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Locale;

public class ResumenBasico extends AppCompatActivity {

    private TextView tvBalance, tvIngresos, tvGastos, tvTitulo;
    private ImageButton btnVolver;
    private FirebaseFirestore db;
    private String uid, mes, anio;
    private double totalIngresos = 0, totalGastos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_basico);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        mes = getIntent().getStringExtra("MES_SELECCIONADO");
        anio = getIntent().getStringExtra("ANIO_SELECCIONADO");

        tvBalance = findViewById(R.id.tvBalanceTotal);
        tvIngresos = findViewById(R.id.tvTotalIngresosBasico);
        tvGastos = findViewById(R.id.tvTotalGastosBasico);
        tvTitulo = findViewById(R.id.tvTituloResumen);
        btnVolver = findViewById(R.id.btnVolverResumenBasico);

        if (mes != null && anio != null) {
            tvTitulo.setText("RESUMEN " + mes.toUpperCase() + " " + anio);
        }

        btnVolver.setOnClickListener(v -> finish());
        obtenerDatosDeFirebase();
    }

    private void obtenerDatosDeFirebase() {
        if (uid == null) return;

        db.collection("Usuarios").document(uid)
                .collection(anio).document(mes)
                .collection("Ingresos").get().addOnSuccessListener(queryIngresos -> {

                    totalIngresos = 0;
                    for (QueryDocumentSnapshot doc : queryIngresos) {
                        if (doc.contains("cantidad")) {
                            totalIngresos += doc.getDouble("cantidad");
                        }
                    }

                    db.collection("Usuarios").document(uid)
                            .collection(anio).document(mes)
                            .collection("Gastos").get().addOnSuccessListener(queryGastos -> {

                                totalGastos = 0;
                                for (QueryDocumentSnapshot doc : queryGastos) {
                                    if (doc.contains("cantidad")) {
                                        totalGastos += doc.getDouble("cantidad");
                                    }
                                }

                                calcularYMostrar();

                            }).addOnFailureListener(e -> Toast.makeText(this, "Error al cargar gastos", Toast.LENGTH_SHORT).show());

                }).addOnFailureListener(e -> Toast.makeText(this, "Error al cargar ingresos", Toast.LENGTH_SHORT).show());
    }

    private void calcularYMostrar() {
        double balanceFinal = totalIngresos - totalGastos;

        tvIngresos.setText(String.format(Locale.US, "%.2f €", totalIngresos));
        tvGastos.setText(String.format(Locale.US, "%.2f €", totalGastos));
        tvBalance.setText(String.format(Locale.US, "%.2f €", balanceFinal));

        if (balanceFinal < 0) {
            tvBalance.setTextColor(Color.parseColor("#C62828"));
        } else {
            tvBalance.setTextColor(Color.parseColor("#2E7D32"));
        }

        // --- LÓGICA DE NOTIFICACIÓN ---
        if (balanceFinal < 50) {
            enviarAvisoSaldoBajo(balanceFinal);
        }
    }

    private void enviarAvisoSaldoBajo(double saldo) {
        String channelId = "pecunia_alertas_basico";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(channelId, "Alertas Pecunia", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(canal);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.cerditoverde)
                .setContentTitle("¡Aviso de Pecunia!")
                .setContentText("Tu balance es de " + String.format("%.2f", saldo) + "€. ¡Ponte un límite!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify(2, builder.build());
    }
}
