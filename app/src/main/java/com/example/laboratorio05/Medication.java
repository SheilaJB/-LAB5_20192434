package com.example.laboratorio05;

import android.net.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Objects;

public class Medication {
    private String name;
    private String type;
    private String dose;
    private int frequencyHours;
    private String startDate;
    private String startTime;

    // Constructor vacío necesario para Gson (deserialización desde JSON)
    public Medication() {}

    // Constructor completo
    public Medication(String name, String type, String dose, int frequencyHours,
                      String startDate, String startTime) {
        this.name = name;
        this.type = type;
        this.dose = dose;
        this.frequencyHours = frequencyHours;
        this.startDate = startDate;
        this.startTime = startTime;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDose() {
        return dose;
    }

    public int getFrequencyHours() {
        return frequencyHours;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public void setFrequencyHours(int frequencyHours) {
        this.frequencyHours = frequencyHours;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    // Método para obtener el texto de la acción sugerida para notificaciones
    public String getSuggestedAction() {
        switch (type.toLowerCase()) {
            case "pastilla":
                return "Tomar " + dose;
            case "jarabe":
                return "Tomar " + dose;
            case "ampolla":
                return "Aplicar " + dose;
            case "capsula":
            case "cápsula":
                return "Tomar " + dose;
            default:
                return "Tomar " + dose;
        }
    }

    // Método para obtener el canal de notificación según el tipo
    public String getNotificationChannel() {
        switch (type.toLowerCase()) {
            case "pastilla":
                return "channel_pastilla";
            case "jarabe":
                return "channel_jarabe";
            case "ampolla":
                return "channel_ampolla";
            case "capsula":
            case "cápsula":
                return "channel_capsula";
            default:
                return "channel_pastilla";
        }
    }

    // Método para obtener el icono según el tipo de medicamento
    public int getNotificationIcon() {
        switch (type.toLowerCase()) {
            case "pastilla":
                return R.drawable.ic_pill; // Reemplazar con tus iconos
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

    // Método para generar un ID único de notificación
    public int getNotificationId() {
        return (name + type).hashCode();
    }

    // Método para obtener la información completa del medicamento (para mostrar en RecyclerView)
    public String getFullInfo() {
        return name + " - " + type + " (" + dose + ") - Cada " + frequencyHours + " horas";
    }

    // Método para obtener fecha y hora de inicio completas
    public String getStartDateTime() {
        return startDate + " " + startTime;
    }

    // Método para obtener el próximo tiempo de notificación (en milisegundos)
    public long getNextNotificationTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date startDateTime = sdf.parse(getStartDateTime());
            if (startDateTime != null) {
                long startTime = startDateTime.getTime();
                long currentTime = System.currentTimeMillis();

                // Si la hora de inicio ya pasó, calcular la próxima dosis
                if (currentTime > startTime) {
                    long intervalMillis = frequencyHours * 60 * 60 * 1000L;
                    long timeSinceStart = currentTime - startTime;
                    long cyclesPassed = timeSinceStart / intervalMillis;
                    return startTime + ((cyclesPassed + 1) * intervalMillis);
                } else {
                    return startTime;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
        return System.currentTimeMillis();
    }

    // Método toString para debugging
    @Override
    public String toString() {
        return "Medication{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", dose='" + dose + '\'' +
                ", frequencyHours=" + frequencyHours +
                ", startDate='" + startDate + '\'' +
                ", startTime='" + startTime + '\'' +
                '}';
    }

    // Método equals para comparar medicamentos
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Medication that = (Medication) obj;
        return frequencyHours == that.frequencyHours &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(dose, that.dose) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(startTime, that.startTime);
    }

    // Método hashCode
    @Override
    public int hashCode() {
        return Objects.hash(name, type, dose, frequencyHours, startDate, startTime);
    }
}
