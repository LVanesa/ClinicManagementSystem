package models.payment;

import models.person.Patient;
import models.treatment.Treatment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Payment {
    private LocalDateTime paymentDate;
    private double totalAmount;
    private Patient patient;
    private List<Treatment> treatments;

    public Payment(LocalDateTime paymentDate, double totalAmount, Patient patient) {
        this.paymentDate = paymentDate;
        this.totalAmount = totalAmount;
        this.patient = patient;
        this.treatments = new ArrayList<>();
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<Treatment> getTreatments() {
        return treatments;
    }

    public void setTreatments(List<Treatment> treatments) {
        this.treatments = treatments;
    }
    @Override
    public String toString() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("Patient: ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("\n");
        sb.append("Payment Date: ").append(paymentDate.format(formatter)).append("\n");
        sb.append("Total Amount: ").append(totalAmount).append("$").append("\n");
        sb.append("Treatments:");
        for (Treatment treatment : treatments) {
            sb.append("\n\t").append(treatment.getName()).append(": ").append(treatment.getPrice()).append("$");
        }
        return sb.toString();
    }



}
