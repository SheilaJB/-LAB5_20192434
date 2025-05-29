package com.example.laboratorio05;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MedicamentoActivity extends AppCompatActivity {
    private RecyclerView recyclerViewMedications;
    private MaterialButton btnAddMedication;
    private LinearLayout layoutEmptyState;
    private TextView tvNoMedications;
    private ImageView btnBack;

    private MedicationAdapter medicationAdapter;
    private List<Medication> medicationList;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "MedicationAppPrefs";
    private static final String KEY_MEDICATIONS = "medications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicamento);

        initializeViews();
        initializeSharedPreferences();
        setupRecyclerView();
        setupClickListeners();
        loadMedications();
    }
    private void initializeViews() {
        recyclerViewMedications = findViewById(R.id.recyclerViewMedications);
        btnAddMedication = findViewById(R.id.btnAddMedication);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvNoMedications = findViewById(R.id.tvNoMedications);
        btnBack = findViewById(R.id.btnBack);
    }
    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    }

    private void setupRecyclerView() {
        medicationList = new ArrayList<>();
        medicationAdapter = new MedicationAdapter(medicationList, this);
        recyclerViewMedications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMedications.setAdapter(medicationAdapter);
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        btnAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(MedicamentoActivity.this, RegistrarMedicamentoActivity.class);
            startActivity(intent);
        });
    }

    private void loadMedications() {
        String medicationsJson = sharedPreferences.getString(KEY_MEDICATIONS, "");

        if (!medicationsJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Medication>>(){}.getType();
                List<Medication> savedMedications = gson.fromJson(medicationsJson, listType);

                if (savedMedications != null && !savedMedications.isEmpty()) {
                    medicationList.clear();
                    medicationList.addAll(savedMedications);
                    medicationAdapter.notifyDataSetChanged();

                    recyclerViewMedications.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                } else {
                    showEmptyState();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showEmptyState();
            }
        } else {
            showEmptyState();
        }
    }

    private void showEmptyState() {
        recyclerViewMedications.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }


    public void deleteMedication(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar medicamento")
                .setMessage("¿Estás seguro de que deseas eliminar este medicamento?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    medicationList.remove(position);
                    medicationAdapter.notifyItemRemoved(position);
                    saveMedicationsToPreferences();

                    if (medicationList.isEmpty()) {
                        showEmptyState();
                    }

                    Toast.makeText(this, "Medicamento eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveMedicationsToPreferences() {
        Gson gson = new Gson();
        String medicationsJson = gson.toJson(medicationList);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MEDICATIONS, medicationsJson);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedications();
    }
}