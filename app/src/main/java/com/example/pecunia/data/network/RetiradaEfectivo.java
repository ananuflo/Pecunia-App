package com.example.pecunia.data.network;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.pecunia.R;
import com.example.pecunia.activities.forms.FormularioGasto;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class RetiradaEfectivo extends AppCompatActivity {

    private String mes, anio;
    private TextView tvLista;
    private Button btnMapaGeneral;
    private FusedLocationProviderClient fusedLocationClient;

    // Tu API KEY de Geoapify (limpia de espacios)
    private final String API_KEY = "958e9040336247aaa460b254bfd6c166".trim();

    private double ultimaLat = 0, ultimaLon = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efectivo);

        mes = getIntent().getStringExtra("MES_SELECCIONADO");
        anio = getIntent().getStringExtra("ANIO_SELECCIONADO");

        tvLista = findViewById(R.id.tvEstadoBusqueda);
        btnMapaGeneral = findViewById(R.id.btnMapaGeneral);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvLista.setText("Buscando señal GPS...");

        findViewById(R.id.btnVolverRetirada).setOnClickListener(v -> finish());

        findViewById(R.id.btnIrAFormularioRetirada).setOnClickListener(v -> {
            Intent i = new Intent(this, FormularioGasto.class);
            i.putExtra("MES_SELECCIONADO", mes);
            i.putExtra("ANIO_SELECCIONADO", anio);
            i.putExtra("CATEGORIA", "Retirada Efectivo");
            startActivity(i);
        });

        btnMapaGeneral.setOnClickListener(v -> {
            if (ultimaLat != 0 && ultimaLon != 0) {
                // Abrir Google Maps con búsqueda de cajeros en la zona
                Uri gmmIntentUri = Uri.parse("geo:" + ultimaLat + "," + ultimaLon + "?q=atms");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Esperando ubicación...", Toast.LENGTH_SHORT).show();
            }
        });

        comprobarPermisos();
    }

    private void comprobarPermisos() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            obtenerLocalizacion();
        }
    }

    private void obtenerLocalizacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                ultimaLat = location.getLatitude();
                ultimaLon = location.getLongitude();
                consultarGeoapify(ultimaLat, ultimaLon);
            } else {
                tvLista.setText("No se puede obtener la ubicación. Verifica que el GPS esté activo.");
            }
        });
    }

    private void consultarGeoapify(double lat, double lon) {
        if (lat == 0 || lon == 0) return;

        OkHttpClient client = new OkHttpClient();

        // 1. Construimos los parámetros por separado para evitar errores de formato
        String coordenadas = lon + "," + lat; // Geoapify exige Longitud,Latitud
        String radio = "2000";

        // 2. Construimos la URL usando concatenación limpia
        // IMPORTANTE: Asegurar de que no haya espacios en blanco en la API_KEY
        String url = "https://api.geoapify.com/v2/places?categories=service.financial.atm" +
                "&filter=circle:" + lon + "," + lat + ",2000" +
                "&bias=proximity:" + lon + "," + lat +
                "&limit=10" +
                "&apiKey=" + API_KEY.trim();

        Log.d("PECUNIA_URL", "URL Final: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> tvLista.setText("Fallo de red: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Si sale Error 400, vamos a leer qué dice el cuerpo del error
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Sin detalle";
                    Log.e("PECUNIA_ERROR", "Cuerpo del error: " + errorBody);
                    runOnUiThread(() -> tvLista.setText("Error 400: Revisa la consola (Logcat) para el detalle."));
                    return;
                }

                try {
                    String res = response.body().string();
                    JSONObject json = new JSONObject(res);
                    JSONArray features = json.getJSONArray("features");

                    StringBuilder sb = new StringBuilder();
                    if (features.length() == 0) {
                        sb.append("No se han encontrado cajeros en 2km.");
                    } else {
                        for (int i = 0; i < features.length(); i++) {
                            JSONObject prop = features.getJSONObject(i).getJSONObject("properties");
                            String banco = prop.optString("name", "Cajero");
                            String dist = prop.optString("distance", "?"); // distancia en metros

                            sb.append("🏦 ").append(banco.toUpperCase()).append("\n");
                            sb.append("📍 Aprox. a ").append(dist).append(" metros\n");
                            sb.append("----------------------------\n\n");
                        }
                    }

                    String finalResult = sb.toString();
                    runOnUiThread(() -> tvLista.setText(finalResult));

                } catch (Exception e) {
                    runOnUiThread(() -> tvLista.setText("Error al leer datos."));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerLocalizacion();
        }
    }
}
