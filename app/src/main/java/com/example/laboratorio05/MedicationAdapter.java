package com.example.laboratorio05;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private List<Medication> medicationList;
    private MedicamentoActivity context;

    public MedicationAdapter(List<Medication> medicationList, MedicamentoActivity context) {
        this.medicationList = medicationList;
        this.context = context;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.bind(medication, position);
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public class MedicationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivMedicationType;
        private TextView tvMedicationName;
        private TextView tvTypeAndDose;
        private TextView tvFrequency;
        private TextView tvStartDateTime;
        private ImageView btnDelete;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedicationType = itemView.findViewById(R.id.ivMedicationType);
            tvMedicationName = itemView.findViewById(R.id.tvMedicationName);
            tvTypeAndDose = itemView.findViewById(R.id.tvTypeAndDose);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvStartDateTime = itemView.findViewById(R.id.tvStartDateTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Medication medication, int position) {
            tvMedicationName.setText(medication.getName());

            String typeAndDose = medication.getDose() + " " + medication.getType() + " - Cada " +
                    medication.getFrequencyHours() + " horas";
            tvTypeAndDose.setText(typeAndDose);

            tvFrequency.setText("Cada " + medication.getFrequencyHours() + " horas");

            tvStartDateTime.setText("Desde: " + medication.getStartDate() + " " + medication.getStartTime());

            setMedicationIcon(medication.getType());

            btnDelete.setOnClickListener(v -> context.deleteMedication(position));
        }

        private void setMedicationIcon(String type) {
            int iconResource = R.drawable.ic_pill; // Default icon

            switch (type.toLowerCase()) {
                case "pastilla":
                    iconResource = R.drawable.ic_pill;
                    break;
                case "jarabe":
                    iconResource = R.drawable.ic_pill;
                    break;
                case "ampolla":
                    iconResource = R.drawable.ic_pill;
                    break;
                case "cápsula":
                    iconResource = R.drawable.ic_pill;
                    break;
            }

            ivMedicationType.setImageResource(iconResource);
        }
    }
}
