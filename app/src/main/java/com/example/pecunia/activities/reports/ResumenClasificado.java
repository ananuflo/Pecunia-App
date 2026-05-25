package com.example.pecunia.activities.reports;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pecunia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ResumenClasificado extends AppCompatActivity {

    private LinearLayout contenedor;
    private TextView tvTitulo;
    private FirebaseFirestore db;
    private String uid, mes, anio, categoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_clasificado);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        categoria = getIntent().getStringExtra("CATEGORIA_SELECCIONADA");
        mes = getIntent().getStringExtra("MES_SELECCIONADO");
        anio = getIntent().getStringExtra("ANIO_SELECCIONADO");

        // CORRECCIÓN DE SEGURIDAD: Sincronizar el nombre con la etiqueta de la base de datos
        if ("Retirada Efectivo".equals(categoria)) {
            categoria = "Efectivo";
        }

        tvTitulo = findViewById(R.id.tvTituloDetalle);
        if (tvTitulo != null && categoria != null) {
            tvTitulo.setText("DETALLE: " + categoria.toUpperCase());
        }

        contenedor = findViewById(R.id.contenedorGastosDetallados);

        findViewById(R.id.btnVolverAtras).setOnClickListener(v -> finish());

        obtenerDetalles();
    }

    private void obtenerDetalles() {
        db.collection("Usuarios").document(uid).collection(anio).document(mes)
                .collection("Gastos")
                .whereEqualTo("categoria", categoria)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    contenedor.removeAllViews();
                    if (querySnapshot.isEmpty()) {
                        // Si la lista está vacía tras un borrado, cerramos la pantalla automáticamente
                        Toast.makeText(this, "No quedan más gastos en esta categoría", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String idDoc = doc.getId();
                        String fecha = doc.getString("fecha");
                        String desc = doc.getString("descripcion");
                        Double cant = doc.getDouble("cantidad");
                        añadirFila(idDoc, fecha, desc, cant);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener desglose", Toast.LENGTH_SHORT).show();
                });
    }

    private void añadirFila(String idDoc, String fecha, String desc, Double cant) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(0, 25, 0, 25);
        fila.setWeightSum(10);
        fila.setGravity(Gravity.CENTER_VERTICAL);

        // Fecha
        TextView tvF = new TextView(this);
        tvF.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.5f));
        tvF.setText(fecha);
        tvF.setTextSize(12);

        // Descripción
        TextView tvD = new TextView(this);
        tvD.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4f));
        tvD.setText(desc);
        tvD.setTextColor(Color.BLACK);
        tvD.setTypeface(null, Typeface.BOLD);

        // Cantidad
        TextView tvC = new TextView(this);
        tvC.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.5f));
        if (cant == null) cant = 0.0;
        tvC.setText(String.format("-%.2f€", cant));
        tvC.setGravity(Gravity.END);
        tvC.setTextColor(Color.parseColor("#C62828"));

        // Botón Borrar (Papelera)
        ImageButton btnBorrar = new ImageButton(this);
        btnBorrar.setLayoutParams(new LinearLayout.LayoutParams(0, 60, 1f));
        btnBorrar.setImageResource(android.R.drawable.ic_menu_delete);
        btnBorrar.setBackgroundColor(Color.TRANSPARENT);
        btnBorrar.setColorFilter(Color.GRAY);

        btnBorrar.setOnClickListener(v -> confirmarBorrado(idDoc));

        fila.addView(tvF);
        fila.addView(tvD);
        fila.addView(tvC);
        fila.addView(btnBorrar);

        contenedor.addView(fila);

        View divisor = new View(this);
        divisor.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divisor.setBackgroundColor(Color.parseColor("#DDDDDD"));
        contenedor.addView(divisor);
    }

    private void confirmarBorrado(String idDoc) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Gasto")
                .setMessage("¿Estás seguro de que quieres borrar este registro?")
                .setPositiveButton("Sí, borrar", (dialog, which) -> {
                    db.collection("Usuarios").document(uid).collection(anio).document(mes)
                            .collection("Gastos").document(idDoc)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Gasto eliminado", Toast.LENGTH_SHORT).show();
                                obtenerDetalles(); // Vuelve a consultar Firebase para actualizar la vista
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "No se pudo eliminar el gasto", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
