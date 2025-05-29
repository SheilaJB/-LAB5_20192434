package com.example.laboratorio05;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeSharedPreferences();
        setupImagePicker();
        checkFirstTimeAndSetupGreeting();
        loadSavedData();
        setupClickListeners();
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

        // Crear EditText para ingresar el nombre
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        input.setHint("Ingresa tu nombre");

        // Configurar padding para el EditText
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String userName = input.getText().toString().trim();
            if (!userName.isEmpty()) {
                saveUserName(userName);
                updateDynamicGreeting();

                // Marcar que ya no es primera vez
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

        // Cargar imagen de perfil
        loadProfileImage();
    }

    private void setupClickListeners() {
        // Click en imagen para seleccionar nueva imagen
        ivProfileImage.setOnClickListener(v -> openImagePicker());

        // Click en botón "Ver mis medicamentos"
        btnViewMedications.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MedicamentoActivity.class);
            startActivity(intent);
        });

        // Click en botón "Configuraciones"
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfiguracionActivity.class);
            startActivity(intent);
        });

        // Long click en saludo para cambiar nombre
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

        // Cargar nombre actual
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
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
}