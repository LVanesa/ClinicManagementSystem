package models.person;

import models.address.Address;
import models.treatment.TreatmentCategory;

import java.util.*;

import java.util.HashSet;
import java.util.Set;

public class Doctor extends Person {
    private Set<TreatmentCategory> specializations;

    public Doctor(String firstName, String lastName, String email, Address address, String phoneNumber) {
        super(firstName, lastName, email, address, phoneNumber);
        specializations = new HashSet<>();
    }

    public Set<TreatmentCategory> getSpecializations() {
        return specializations;
    }
    public void deleteSpecializations() {
        specializations.clear();
    }

    public void addSpecialization(TreatmentCategory specialization) {
        specializations.add(specialization);
    }

    public void removeSpecialization(TreatmentCategory specialization) {
        specializations.remove(specialization);
    }

    public String serializeSpecializations() {

        StringBuilder specializationsString = new StringBuilder();
        for (TreatmentCategory category : specializations) {
            if (specializationsString.length() > 0) {
                specializationsString.append(", ");
            }
            specializationsString.append(category.name());
        }
        return specializationsString.toString();
    }

    public static Set<TreatmentCategory> deserializeSpecializations(String serializedSpecializations) {
        Set<TreatmentCategory> specializations = new HashSet<>();
        String[] specializationStrings = serializedSpecializations.split(",");
        for (String specializationString : specializationStrings) {
            try {
                TreatmentCategory specialization = TreatmentCategory.valueOf(specializationString.trim());
                specializations.add(specialization);
            } catch (IllegalArgumentException e) {
                // Ignorăm specializările nevalide
                System.out.println("Ignoring invalid specialization: " + specializationString);
            }
        }
        return specializations;
    }

    public void setSpecializations(Set<TreatmentCategory> specializations) {
        this.specializations = specializations;
    }

    @Override
    public String toString() {
        StringBuilder specializationsString = new StringBuilder();
        for (TreatmentCategory category : specializations) {
            if (specializationsString.length() > 0) {
                specializationsString.append(", ");
            }
            specializationsString.append(category.name());
        }

        return "Doctor: " + getFirstName() + " " + getLastName() +
                "\nEmail: " + getEmail() +
                "\nAddress: " + getAddress() +
                "\nPhone number: " + getPhoneNumber() +
                "\nSpecializations: " + specializationsString.toString();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return firstName.equals(doctor.firstName) &&
                lastName.equals(doctor.lastName) &&
                email.equals(doctor.email) &&
                phoneNumber.equals(doctor.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, phoneNumber);
    }

}

