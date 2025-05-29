package com.example.laboratorio05;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ConfiguracionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextInputEditText etMotivationalMessage;
    private TextInputEditText etFrequencyHours;
    private MaterialButton btnSaveMotivational;
    private MaterialButton btnStopMotivational;
    private MaterialButton btnChangeUserName;
    private MaterialButton btnTestNotification;
    private TextView tvCurrentConfig;

    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "MedicationAppPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_MOTIVATIONAL_MESSAGE = "motivational_message";
    private static final String KEY_MOTIVATIONAL_FREQUENCY = "motivational_frequency";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracion);

        // Aplicar window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeSharedPreferences();
        loadCurrentConfiguration();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etMotivationalMessage = findViewById(R.id.etMotivationalMessage);
        etFrequencyHours = findViewById(R.id.etFrequencyHours);
        btnSaveMotivational = findViewById(R.id.btnSaveMotivational);
        btnStopMotivational = findViewById(R.id.btnStopMotivational);
        btnChangeUserName = findViewById(R.id.btnChangeUserName);
        btnTestNotification = findViewById(R.id.btnTestNotification);
        tvCurrentConfig = findViewById(R.id.tvCurrentConfig);
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    }

    private void loadCurrentConfiguration() {
        // Cargar mensaje motivacional actual
        String currentMessage = sharedPreferences.getString(KEY_MOTIVATIONAL_MESSAGE,
                "¡Hoy es un buen día para cuidar tu salud!");
        etMotivationalMessage.setText(currentMessage);

        // Cargar frecuencia actual
        int currentFrequency = sharedPreferences.getInt(KEY_MOTIVATIONAL_FREQUENCY, 0);
        if (currentFrequency > 0) {
            etFrequencyHours.setText(String.valueOf(currentFrequency));
        }

        // Mostrar configuración actual
        updateCurrentConfigDisplay();
    }

    private void updateCurrentConfigDisplay() {
        String userName = sharedPreferences.getString(KEY_USER_NAME, "Usuario");
        String message = sharedPreferences.getString(KEY_MOTIVATIONAL_MESSAGE,
                "¡Hoy es un buen día para cuidar tu salud!");
        int frequency = sharedPreferences.getInt(KEY_MOTIVATIONAL_FREQUENCY, 0);

        StringBuilder configText = new StringBuilder();
        configText.append("👤 Usuario: ").append(userName).append("\n\n");
        configText.append("💬 Mensaje actual: ").append(message).append("\n\n");

        if (frequency > 0) {
            configText.append("⏰ Frecuencia: Cada ").append(frequency).append(" horas\n");
            configText.append("📱 Estado: Activo");
        } else {
            configText.append("📱 Estado: Notificaciones desactivadas");
        }

        tvCurrentConfig.setText(configText.toString());
    }

    private void setupClickListeners() {
        // Botón de regreso
        btnBack.setOnClickListener(v -> finish());

        // Guardar configuración motivacional
        btnSaveMotivational.setOnClickListener(v -> saveMotivationalConfiguration());

        // Detener notificaciones motivacionales
        btnStopMotivational.setOnClickListener(v -> stopMotivationalNotifications());

        // Cambiar nombre de usuario
        btnChangeUserName.setOnClickListener(v -> showChangeUserNameDialog());

        // Probar notificación (para testing)
        btnTestNotification.setOnClickListener(v -> testMotivationalNotification());
    }

    private void saveMotivationalConfiguration() {
        String mensaje = etMotivationalMessage.getText().toString().trim();
        String frecuenciaStr = etFrequencyHours.getText().toString().trim();

        // Validaciones
        if (mensaje.isEmpty()) {
            etMotivationalMessage.setError("Ingrese un mensaje motivacional");
            etMotivationalMessage.requestFocus();
            return;
        }

        if (frecuenciaStr.isEmpty()) {
            etFrequencyHours.setError("Ingrese la frecuencia en horas");
            etFrequencyHours.requestFocus();
            return;
        }

        int frecuenciaHoras;
        try {
            frecuenciaHoras = Integer.parseInt(frecuenciaStr);
            if (frecuenciaHoras <= 0 || frecuenciaHoras > 24) {
                etFrequencyHours.setError("La frecuencia debe estar entre 1 y 24 horas");
                etFrequencyHours.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etFrequencyHours.setError("Ingrese un número válido");
            etFrequencyHours.requestFocus();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MOTIVATIONAL_MESSAGE, mensaje);
        editor.putInt(KEY_MOTIVATIONAL_FREQUENCY, frecuenciaHoras);
        editor.apply();

        programarNotificacionesMotivacionales(mensaje, frecuenciaHoras);

        updateCurrentConfigDisplay();

        Toast.makeText(this,
                "✅ Configuración guardada.\nRecibirás mensajes cada " + frecuenciaHoras + " horas",
                Toast.LENGTH_LONG).show();

        Log.d("ConfiguracionActivity", "Configuración motivacional guardada: " + mensaje +
                " cada " + frecuenciaHoras + " horas");
    }

    private void programarNotificacionesMotivacionales(String mensaje, int frecuenciaHoras) {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("action", "program_motivational");
            intent.putExtra("frequency", frecuenciaHoras);


            Log.d("ConfiguracionActivity", "Configuración guardada para programar notificaciones");

        } catch (Exception e) {
            Log.e("ConfiguracionActivity", "Error al programar notificaciones: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error al programar notificaciones", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopMotivationalNotifications() {
        new AlertDialog.Builder(this)
                .setTitle("Detener Notificaciones")
                .setMessage("¿Estás seguro de que deseas detener las notificaciones motivacionales?")
                .setPositiveButton("Sí, detener", (dialog, which) -> {
                    try {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(KEY_MOTIVATIONAL_FREQUENCY);
                        editor.apply();

                        etFrequencyHours.setText("");

                        updateCurrentConfigDisplay();

                        Toast.makeText(this, "❌ Notificaciones motivacionales detenidas", Toast.LENGTH_SHORT).show();
                        Log.d("ConfiguracionActivity", "Notificaciones motivacionales detenidas");

                    } catch (Exception e) {
                        Log.e("ConfiguracionActivity", "Error al detener notificaciones: " + e.getMessage());
                        Toast.makeText(this, "Error al detener notificaciones", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showChangeUserNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar Nombre de Usuario");
        builder.setMessage("Ingresa tu nuevo nombre:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

        String currentName = sharedPreferences.getString(KEY_USER_NAME, "Usuario");
        input.setText(currentName);
        input.setSelection(currentName.length());

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        builder.setView(input);

        builder.setPositiveButton("Cambiar", (dialog, which) -> {
            String newUserName = input.getText().toString().trim();
            if (!newUserName.isEmpty()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_USER_NAME, newUserName);
                editor.apply();

                updateCurrentConfigDisplay();

                Toast.makeText(this, "✅ Nombre cambiado a: " + newUserName, Toast.LENGTH_SHORT).show();
                Log.d("ConfiguracionActivity", "Nombre de usuario cambiado a: " + newUserName);
            } else {
                Toast.makeText(this, "❌ El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void testMotivationalNotification() {
        String mensaje = etMotivationalMessage.getText().toString().trim();

        if (mensaje.isEmpty()) {
            Toast.makeText(this, "Primero ingresa un mensaje motivacional", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Crear Intent para MainActivity con acción de test
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("action", "test_motivational");
            startActivity(intent);

            Toast.makeText(this, "🔔 Notificación de prueba enviada", Toast.LENGTH_SHORT).show();
            Log.d("ConfiguracionActivity", "Notificación de prueba solicitada");

        } catch (Exception e) {
            Log.e("ConfiguracionActivity", "Error al enviar notificación de prueba: " + e.getMessage());
            Toast.makeText(this, "Error al enviar notificación de prueba", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentConfiguration();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ConfiguracionActivity", "ConfiguracionActivity destruida");
    }
}