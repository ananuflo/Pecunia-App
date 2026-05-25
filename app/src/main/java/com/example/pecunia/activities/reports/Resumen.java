package com.example.pecunia.activities.reports;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.pecunia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class Resumen extends AppCompatActivity {

    private TextView tvBalance, tvTotalIng, tvTotalGas;
    private FirebaseFirestore db;
    private String uid, mes, anio;
    private Map<String, Double> mapaGastos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mes = getIntent().getStringExtra("MES_SELECCIONADO");
        anio = getIntent().getStringExtra("ANIO_SELECCIONADO");

        tvBalance = findViewById(R.id.tvBalancePremium);
        tvTotalIng = findViewById(R.id.tvTotalIngresosPremium);
        tvTotalGas = findViewById(R.id.tvTotalGastosPremium);

        findViewById(R.id.btnVolverAtras).setOnClickListener(v -> finish());

        inicializarMapa();
        actualizarInterfazTabla();
        // Se ha quitado la carga de aquí para moverla al ciclo onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        // SOLUCIÓN: Cada vez que vuelves de borrar un gasto, este método se activa y actualiza las cuentas automáticamente
        cargarDatosDeFirebase();
    }

    private void inicializarMapa() {
        String[] categoriasBase = {"Alimentación", "Salud", "Ocio", "Transporte", "Compras", "Hogar", "Retirada Efectivo"};
        mapaGastos.clear();

        for (String c : categoriasBase) {
            mapaGastos.put(c, 0.0);
        }
    }

    private void cargarDatosDeFirebase() {
        db.collection("Usuarios").document(uid).collection(anio).document(mes)
                .collection("Ingresos").get().addOnSuccessListener(queryIngresos -> {
                    double totalI = 0;
                    for (QueryDocumentSnapshot doc : queryIngresos) {
                        totalI += doc.getDouble("cantidad");
                    }
                    tvTotalIng.setText(String.format("Ingresos: %.2f €", totalI));
                    final double finalTotalI = totalI;

                    db.collection("Usuarios").document(uid).collection(anio).document(mes)
                            .collection("Gastos").get().addOnSuccessListener(queryGastos -> {
                                double totalG = 0;
                                inicializarMapa();

                                for (QueryDocumentSnapshot doc : queryGastos) {
                                    Double cant = doc.getDouble("cantidad");
                                    String cat = doc.getString("categoria");
                                    if (cant == null) cant = 0.0;
                                    totalG += cant;

                                    if (cat != null && mapaGastos.containsKey(cat)) {
                                        mapaGastos.put(cat, mapaGastos.get(cat) + cant);
                                    }
                                }

                                tvTotalGas.setText(String.format("Gastos: %.2f €", totalG));
                                double balance = finalTotalI - totalG;
                                tvBalance.setText(String.format("%.2f €", balance));
                                tvBalance.setTextColor(balance >= 0 ? Color.parseColor("#2E7D32") : Color.RED);

                                db.collection("Usuarios").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                                    double limitePersonalizado = 50.0;

                                    if (documentSnapshot.exists() && documentSnapshot.contains("limiteAviso")) {
                                        limitePersonalizado = documentSnapshot.getDouble("limiteAviso");
                                    }

                                    if (balance < limitePersonalizado) {
                                        enviarAvisoSaldoBajo(balance, limitePersonalizado);
                                    }
                                });

                                actualizarInterfazTabla();
                            });
                });
    }

    private void enviarAvisoSaldoBajo(double saldo, double limite) {
        String channelId = "pecunia_alertas";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(channelId, "Alertas Pecunia", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(canal);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.cerditoverde)
                .setContentTitle("¡Saldo Bajo!")
                .setContentText("Saldo inferior a " + String.format("%.2f", limite) + "€. ¡Ponte un límite!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify(1, builder.build());
    }

    private void actualizarInterfazTabla() {
        configurarFila(R.id.filaComida, R.id.tvTotalComida, "Alimentación");
        configurarFila(R.id.filaSalud, R.id.tvTotalSalud, "Salud");
        configurarFila(R.id.filaOcio, R.id.tvTotalOcio, "Ocio");
        configurarFila(R.id.filaTransporte, R.id.tvTotalTransporte, "Transporte");
        configurarFila(R.id.filaCompras, R.id.tvTotalCompras, "Compras");
        configurarFila(R.id.filaHogar, R.id.tvTotalHogar, "Hogar");
        configurarFila(R.id.filaEfectivo, R.id.tvTotalEfectivo, "Retirada Efectivo");
    }

    private void configurarFila(int idFila, int idTvTotal, String nombreCat) {
        TableRow fila = findViewById(idFila);
        TextView tvTotal = findViewById(idTvTotal);

        if (fila != null && tvTotal != null) {
            double valor = mapaGastos.getOrDefault(nombreCat, 0.0);
            tvTotal.setText(String.format("%.2f €", valor));
            tvTotal.setTextColor(valor > 0 ? Color.RED : Color.parseColor("#888888"));

            fila.setOnClickListener(v -> {
                Intent i = new Intent(Resumen.this, ResumenClasificado.class);
                i.putExtra("CATEGORIA_SELECCIONADA", nombreCat);
                i.putExtra("MES_SELECCIONADO", mes);
                i.putExtra("ANIO_SELECCIONADO", anio);
                startActivity(i);
            });
        }
    }
}