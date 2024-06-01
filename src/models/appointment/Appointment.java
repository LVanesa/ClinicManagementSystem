package models.appointment;

import models.person.Doctor;
import models.person.Patient;
import models.treatment.Treatment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment implements Comparable<Appointment>{
    private LocalDateTime date;
    private Patient patient;
    private Treatment treatment;
    private Doctor doctor;
    private AppointmentStatus status;

    public Appointment(LocalDateTime date, Patient patient, Treatment treatment, Doctor doctor, AppointmentStatus status) {
        this.date = date;
        this.patient = patient;
        this.treatment = treatment;
        this.doctor = doctor;
        this.status = status;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    @Override
    public int compareTo(Appointment otherAppointment){
        return this.date.compareTo(otherAppointment.getDate());
    }
    @Override
    public String toString() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String formattedDate = date.format(dateFormatter);
        String formattedTime = date.format(timeFormatter);

        return String.format("%-30s %-30s %-30s %-30s %-30s",
                " | Date: " + formattedDate + " " + formattedTime,
                " | Patient: " + patient.getFirstName() + " " + patient.getLastName(),
                " | Treatment: " + treatment.getName(),
                " | Doctor: " + doctor.getFirstName() + " " + doctor.getLastName(),
                " | Status: " + status);
    }
}
