package com.example.laboratorio05;

import android.net.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Medication {
    private String name;
    private String type;
    private String dose;
    private int frequencyHours;
    private String startDate;
    private String startTime;

    public Medication() {}

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

    public int getNotificationIcon() {
        switch (type.toLowerCase()) {
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

    public int getNotificationId() {
        return (name + type).hashCode();
    }

    public String getFullInfo() {
        return name + " - " + type + " (" + dose + ") - Cada " + frequencyHours + " horas";
    }

    public String getStartDateTime() {
        return startDate + " " + startTime;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(name, type, dose, frequencyHours, startDate, startTime);
    }
}
