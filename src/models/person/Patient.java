package models.person;

import models.address.Address;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Patient extends Person{
    private LocalDateTime birthDate;
    private int age;
    private String gender;
    private String medicalHistory;

    public Patient(String firstName, String lastName, String email, Address address, String phoneNumber, LocalDateTime birthDate, int age, String gender, String medicalHistory) {
        super(firstName, lastName, email, address, phoneNumber);
        this.birthDate = birthDate;
        this.age = age;
        this.gender = gender;
        this.medicalHistory = medicalHistory;
    }

    public LocalDateTime getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDateTime birthDate) {
        this.birthDate = birthDate;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
    @Override
    public String toString() {
        // Format the birth date
        String formattedBirthDate = birthDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        // Construct the patient information string
        return "Patient: " + getFirstName() + " " + getLastName() +
                "\nEmail: " + getEmail() +
                "\nAddress: " + getAddress() +
                "\nPhone number: " + getPhoneNumber() +
                "\nBirth date: " + formattedBirthDate +
                "\nAge: " + age +
                "\nGender: " + gender +
                "\nMedical history: " + medicalHistory;
    }
}
