package com.example.laboratorio05;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import static android.Manifest.permission.POST_NOTIFICATIONS;
public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvMotivationalMessage;
    private ImageView ivProfileImage;
    private MaterialButton btnViewMedications;
    private MaterialButton btnSettings;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "MedicationAppPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_MOTIVATIONAL_MESSAGE = "motivational_message";
    private static final String KEY_PROFILE_IMAGE_URI = "profile_image_uri";
    private static final String KEY_IS_FIRST_TIME = "is_first_time";
    private ActivityResultLauncher<Intent> imagePickerLauncher;


    private static final String CHANNEL_PASTILLA = "channel_pastilla";
    private static final String CHANNEL_JARABE = "channel_jarabe";
    private static final String CHANNEL_AMPOLLA = "channel_ampolla";
    private static final String CHANNEL_CAPSULA = "channel_capsula";
    private static final String CHANNEL_MOTIVACIONAL = "channel_motivacional";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initializeViews();
        initializeSharedPreferences();
        setupImagePicker();
        createNotificationChannels();
        checkFirstTimeAndSetupGreeting();
        loadSavedData();
        setupClickListeners();
    }

    public void createNotificationChannels() {
        // Solo para Android >= API 26
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            // Canal para Pastillas
            NotificationChannel channelPastilla = new NotificationChannel(
                    CHANNEL_PASTILLA,
                    "Recordatorios de Pastillas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelPastilla.setDescription("Notificaciones para recordatorios de pastillas");
            channelPastilla.enableVibration(true);

            // Canal para Jarabes
            NotificationChannel channelJarabe = new NotificationChannel(
                    CHANNEL_JARABE,
                    "Recordatorios de Jarabes",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelJarabe.setDescription("Notificaciones para recordatorios de jarabes");
            channelJarabe.enableVibration(true);

            // Canal para Ampollas
            NotificationChannel channelAmpolla = new NotificationChannel(
                    CHANNEL_AMPOLLA,
                    "Recordatorios de Ampollas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelAmpolla.setDescription("Notificaciones para recordatorios de ampollas");
            channelAmpolla.enableVibration(true);

            // Canal para Cápsulas
            NotificationChannel channelCapsula = new NotificationChannel(
                    CHANNEL_CAPSULA,
                    "Recordatorios de Cápsulas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelCapsula.setDescription("Notificaciones para recordatorios de cápsulas");
            channelCapsula.enableVibration(true);

            // Canal para Motivacional
            NotificationChannel channelMotivacional = new NotificationChannel(
                    CHANNEL_MOTIVACIONAL,
                    "Mensajes Motivacionales",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channelMotivacional.setDescription("Notificaciones motivacionales configurables");

            // Registrar canales
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelPastilla);
            notificationManager.createNotificationChannel(channelJarabe);
            notificationManager.createNotificationChannel(channelAmpolla);
            notificationManager.createNotificationChannel(channelCapsula);
            notificationManager.createNotificationChannel(channelMotivacional);

            // Pedir permisos para Android 13+
            askPermission();
        }
    }

    public void askPermission() {
        // Solo para Android >= API 33 (Android 13)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{POST_NOTIFICATIONS},
                    101);
        }
    }

    public void lanzarNotificacionMedicamento(String nombreMedicamento, String tipoMedicamento, String dosis) {
        Intent intent = new Intent(this, MedicamentoActivity.class);
        intent.putExtra("medicamento_nombre", nombreMedicamento);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String channelId = getChannelByMedicationType(tipoMedicamento);

        int iconoMedicamento = getIconByMedicationType(tipoMedicamento);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(iconoMedicamento) // Icono distinto al de la aplicación
                .setContentTitle("Recordatorio: " + nombreMedicamento)
                .setContentText("Tomar " + dosis) // Acción sugerida
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Lanzar notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(generateNotificationId(), builder.build());
        }
    }

    public void lanzarNotificacionMotivacional() {
        String mensajeMotivacional = sharedPreferences.getString(KEY_MOTIVATIONAL_MESSAGE,
                "¡Hoy es un buen día para cuidar tu salud!");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_MOTIVACIONAL)
                .setSmallIcon(R.drawable.ic_heart)
                .setContentTitle("Mensaje Motivacional")
                .setContentText(mensajeMotivacional)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(999, builder.build());
        }
    }

    // MÉTODOS AUXILIARES
    private String getChannelByMedicationType(String tipo) {
        switch (tipo.toLowerCase()) {
            case "pastilla":
                return CHANNEL_PASTILLA;
            case "jarabe":
                return CHANNEL_JARABE;
            case "ampolla":
                return CHANNEL_AMPOLLA;
            case "capsula":
            case "cápsula":
                return CHANNEL_CAPSULA;
            default:
                return CHANNEL_PASTILLA;
        }
    }

    private int getIconByMedicationType(String tipo) {
        switch (tipo.toLowerCase()) {
            case "pastilla":
                return R.drawable.ic_pill;
            case "jarabe":
                return R.drawable.ic_syrup;
            case "ampolla":
                return R.drawable.ic_injection;
            case "capsula":
            case "cápsula":
                return R.drawable.ic_capsule;
            default:
                return R.drawable.ic_pill;
        }
    }

    private int generateNotificationId() {
        return (int) System.currentTimeMillis();
    }


    private void initializeViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvMotivationalMessage = findViewById(R.id.tvMotivationalMessage);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnViewMedications = findViewById(R.id.btnViewMedications);
        btnSettings = findViewById(R.id.btnSettings);
    }
    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    }
    private void checkFirstTimeAndSetupGreeting() {
        boolean isFirstTime = sharedPreferences.getBoolean(KEY_IS_FIRST_TIME, true);
        if (isFirstTime) {
            showWelcomeDialog();
        } else {
            updateDynamicGreeting();
        }
    }

    private void showWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Bienvenido!");
        builder.setMessage("Para personalizar tu experiencia, ¿cuál es tu nombre?");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        input.setHint("Ingresa tu nombre");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        builder.setView(input);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String userName = input.getText().toString().trim();
            if (!userName.isEmpty()) {
                saveUserName(userName);
                updateDynamicGreeting();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_IS_FIRST_TIME, false);
                editor.apply();
                Toast.makeText(this, "¡Hola " + userName + "! Tu nombre ha sido guardado.", Toast.LENGTH_LONG).show();
            } else {
                saveUserName("Usuario");
                updateDynamicGreeting();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_IS_FIRST_TIME, false);
                editor.apply();
            }
        });
        builder.setNegativeButton("Después", (dialog, which) -> {
            saveUserName("Usuario");
            updateDynamicGreeting();
            dialog.cancel();
        });
        builder.setCancelable(false);
        builder.show();
    }
    private void saveUserName(String userName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }
    private void updateDynamicGreeting() {
        String userName = sharedPreferences.getString(KEY_USER_NAME, "Usuario");
        String dynamicGreeting = getDynamicGreetingByTime(userName);
        tvGreeting.setText(dynamicGreeting);
    }
    private String getDynamicGreetingByTime(String userName) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "¡Buenos días, " + userName + "!";
        } else if (hour >= 12 && hour < 18) {
            greeting = "¡Buenas tardes, " + userName + "!";
        } else if (hour >= 18 && hour < 22) {
            greeting = "¡Buenas noches, " + userName + "!";
        } else {
            greeting = "¡Hola, " + userName + "!";
        }
        return greeting;
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            saveImageToInternalStorage(imageUri);
                        }
                    }
                }
        );
    }
    private void loadSavedData() {
        // Cargar mensaje motivacional
        String motivationalMessage = sharedPreferences.getString(KEY_MOTIVATIONAL_MESSAGE,
                "¡Hoy es un buen día para cuidar tu salud!");
        tvMotivationalMessage.setText(motivationalMessage);
        loadProfileImage();
    }

    private void setupClickListeners() {
        ivProfileImage.setOnClickListener(v -> openImagePicker());
        btnViewMedications.setOnClickListener(v -> {
            Log.d("MainActivity", "Botón presionado - navegando a MedicamentoActivity");
            try {
                Intent intent = new Intent(MainActivity.this, MedicamentoActivity.class);
                startActivity(intent);
                Log.d("MainActivity", "Intent enviado correctamente");
            } catch (Exception e) {
                Log.e("MainActivity", "Error al navegar: " + e.getMessage());
                e.printStackTrace();
            }
        });
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfiguracionActivity.class);
            startActivity(intent);
        });
        tvGreeting.setOnLongClickListener(v -> {
            showChangeNameDialog();
            return true;
        });
    }

    private void showChangeNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar nombre");
        builder.setMessage("¿Quieres cambiar tu nombre?");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        String currentName = sharedPreferences.getString(KEY_USER_NAME, "Usuario");
        input.setText(currentName);
        input.setSelection(currentName.length()); // Posicionar cursor al final
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        builder.setView(input);
        builder.setPositiveButton("Cambiar", (dialog, which) -> {
            String newUserName = input.getText().toString().trim();
            if (!newUserName.isEmpty()) {
                saveUserName(newUserName);
                updateDynamicGreeting();
                Toast.makeText(this, "Nombre cambiado a: " + newUserName, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            Intent chooser = Intent.createChooser(intent, "Seleccionar imagen");
            if (chooser.resolveActivity(getPackageManager()) != null) {
                imagePickerLauncher.launch(chooser);
            } else {
                Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setType("image/*");
                if (pickIntent.resolveActivity(getPackageManager()) != null) {
                    imagePickerLauncher.launch(pickIntent);
                } else {
                    Toast.makeText(this, "No se encontró una aplicación para seleccionar imágenes. " +
                            "Por favor, instale Google Fotos o una aplicación de galería.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir selector de imágenes: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveImageToInternalStorage(Uri imageUri) {
        try {
            File imageDir = new File(getFilesDir(), "images");
            if (!imageDir.exists()) {
                imageDir.mkdir();
            }
            String fileName = "profile_image.jpg";
            File imageFile = new File(imageDir, fileName);
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_PROFILE_IMAGE_URI, imageFile.getAbsolutePath());
            editor.apply();
            loadProfileImage();
            Toast.makeText(this, "Imagen guardada correctamente", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadProfileImage() {
        String imagePath = sharedPreferences.getString(KEY_PROFILE_IMAGE_URI, null);
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    ivProfileImage.setImageBitmap(bitmap);
                }
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateDynamicGreeting();
        loadSavedData();
    }
}