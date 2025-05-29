package com.example.laboratorio05;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegistrarMedicamentoActivity extends AppCompatActivity {

    private TextInputEditText etMedicationName, etDose, etFrequency;
    private Spinner spinnerType;
    private TextView tvSelectedDate, tvSelectedTime;
    private Button btnSelectDate, btnSelectTime;
    private MaterialButton btnSave;
    private ImageView btnBack;

    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "MedicationAppPrefs";
    private static final String KEY_MEDICATIONS = "medications";

    private Calendar selectedDateTime;

    private static final String CHANNEL_PASTILLA = "channel_pastilla";
    private static final String CHANNEL_JARABE = "channel_jarabe";
    private static final String CHANNEL_AMPOLLA = "channel_ampolla";
    private static final String CHANNEL_CAPSULA = "channel_capsula";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_medicamento);

        initializeViews();
        setupSpinner();
        setupClickListeners();
        initializeSharedPreferences();
        initializeDateTime();
        createNotificationChannels();
    }

    private void initializeViews() {
        etMedicationName = findViewById(R.id.etMedicationName);
        etDose = findViewById(R.id.etDose);
        etFrequency = findViewById(R.id.etFrequency);
        spinnerType = findViewById(R.id.spinnerType);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnSave = findViewById(R.id.btnSave);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupSpinner() {
        String[] types = {"Pastilla", "Jarabe", "Ampolla", "Cápsula"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveMedication());
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    }

    private void initializeDateTime() {
        selectedDateTime = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));
        tvSelectedTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel channelPastilla = new NotificationChannel(
                    CHANNEL_PASTILLA,
                    "Recordatorios de Pastillas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelPastilla.setDescription("Notificaciones para recordatorios de pastillas");
            channelPastilla.enableVibration(true);
            channelPastilla.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            NotificationChannel channelJarabe = new NotificationChannel(
                    CHANNEL_JARABE,
                    "Recordatorios de Jarabes",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelJarabe.setDescription("Notificaciones para recordatorios de jarabes");
            channelJarabe.enableVibration(true);
            channelJarabe.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            NotificationChannel channelAmpolla = new NotificationChannel(
                    CHANNEL_AMPOLLA,
                    "Recordatorios de Ampollas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelAmpolla.setDescription("Notificaciones para recordatorios de ampollas");
            channelAmpolla.enableVibration(true);
            channelAmpolla.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            // Canal para Cápsulas - IMPORTANCE_HIGH con vibración
            NotificationChannel channelCapsula = new NotificationChannel(
                    CHANNEL_CAPSULA,
                    "Recordatorios de Cápsulas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelCapsula.setDescription("Notificaciones para recordatorios de cápsulas");
            channelCapsula.enableVibration(true);
            channelCapsula.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            // Registrar canales en el sistema
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelPastilla);
            notificationManager.createNotificationChannel(channelJarabe);
            notificationManager.createNotificationChannel(channelAmpolla);
            notificationManager.createNotificationChannel(channelCapsula);

            Log.d("NotificationChannels", "Canales de notificación creados correctamente");
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    tvSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH));

        // No permitir fechas pasadas
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    tvSelectedTime.setText(timeFormat.format(selectedDateTime.getTime()));
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true); // Formato 24 horas

        timePickerDialog.show();
    }

    private void saveMedication() {
        // Obtener valores de los campos
        String name = etMedicationName.getText().toString().trim();
        String dose = etDose.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String frequencyStr = etFrequency.getText().toString().trim();

        // Validaciones
        if (name.isEmpty()) {
            etMedicationName.setError("Ingrese el nombre del medicamento");
            etMedicationName.requestFocus();
            return;
        }

        if (dose.isEmpty()) {
            etDose.setError("Ingrese la dosis");
            etDose.requestFocus();
            return;
        }

        if (frequencyStr.isEmpty()) {
            etFrequency.setError("Ingrese la frecuencia en horas");
            etFrequency.requestFocus();
            return;
        }

        int frequency;
        try {
            frequency = Integer.parseInt(frequencyStr);
            if (frequency <= 0 || frequency > 24) {
                etFrequency.setError("La frecuencia debe estar entre 1 y 24 horas");
                etFrequency.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etFrequency.setError("Ingrese un número válido");
            etFrequency.requestFocus();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String startDate = dateFormat.format(selectedDateTime.getTime());
        String startTime = timeFormat.format(selectedDateTime.getTime());

        Medication medication = new Medication(name, type, dose, frequency, startDate, startTime);

        // Guardar en SharedPreferences
        saveMedicationToPreferences(medication);

        scheduleNotificationsForMedication(medication);

        Toast.makeText(this, "Medicamento '" + name + "' guardado y notificaciones programadas",
                Toast.LENGTH_LONG).show();
        finish();
    }

    private void scheduleNotificationsForMedication(Medication medication) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long firstNotificationTime = selectedDateTime.getTimeInMillis();

        if (firstNotificationTime <= System.currentTimeMillis()) {
            firstNotificationTime = System.currentTimeMillis() + (medication.getFrequencyHours() * 60 * 60 * 1000L);
        }

        Intent intent = new Intent(this, MedicationReminderReceiver.class);
        intent.putExtra("medicamento_nombre", medication.getName());
        intent.putExtra("medicamento_tipo", medication.getType());
        intent.putExtra("medicamento_dosis", medication.getDose());
        intent.putExtra("medicamento_frecuencia", medication.getFrequencyHours());

        int requestCode = medication.getNotificationId();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            // Programar la primera alarma
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        firstNotificationTime,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        firstNotificationTime,
                        pendingIntent
                );
            }

            Log.d("MedicationScheduler", "Notificación programada para: " + medication.getName() +
                    " a las " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(firstNotificationTime));

        } catch (Exception e) {
            Log.e("MedicationScheduler", "Error al programar notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveMedicationToPreferences(Medication newMedication) {
        // Cargar lista existente
        List<Medication> medicationList = loadMedicationsFromPreferences();

        // Agregar nuevo medicamento
        medicationList.add(newMedication);

        // Convertir a JSON y guardar
        Gson gson = new Gson();
        String medicationsJson = gson.toJson(medicationList);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MEDICATIONS, medicationsJson);
        editor.apply();

        Log.d("MedicationStorage", "Medicamento guardado: " + newMedication.getName());
    }
    private List<Medication> loadMedicationsFromPreferences() {
        String medicationsJson = sharedPreferences.getString(KEY_MEDICATIONS, "");

        if (!medicationsJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Medication>>(){}.getType();
                List<Medication> medications = gson.fromJson(medicationsJson, listType);
                return medications != null ? medications : new ArrayList<>();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MedicationStorage", "Error al cargar medicamentos: " + e.getMessage());
            }
        }

        return new ArrayList<>();
    }
/*
    public void cancelNotificationsForMedication(Medication medication) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, MedicationReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                medication.getNotificationId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d("MedicationScheduler", "Notificaciones canceladas para: " + medication.getName());
    } */
}