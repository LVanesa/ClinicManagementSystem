package services;

import database.DatabaseConfiguration;
import models.address.Address;
import models.appointment.Appointment;
import models.appointment.AppointmentStatus;
import models.payment.Payment;
import models.person.Doctor;
import models.person.Patient;
import models.treatment.Treatment;
import models.treatment.TreatmentCategory;
import repositories.*;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Service {
    private static Service instance;
    private Service() { }

    static {
        try {
            instance = new Service();
        } catch (Exception e) {
            throw new RuntimeException("\nException occurred in creating Service singleton instance");
        }
    }

    public static Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    public static List<Patient> patients = new ArrayList<>();
    public static List<Doctor> doctors = new ArrayList<>();
    public static List<Treatment> treatments = new ArrayList<>();
    public static List<Appointment> appointments = new ArrayList<>();
    public static List<Payment> payments = new ArrayList<>();
    PatientRepository patientRepository = PatientRepository.getInstance();
    DoctorRepository doctorRepository = DoctorRepository.getInstance();
    TreatmentRepository treatmentRepository = TreatmentRepository.getInstance();
    AppointmentRepository appointmentRepository = AppointmentRepository.getInstance();
    PaymentRepository paymentRepository = PaymentRepository.getInstance();
    Audit audit = Audit.getInstance();
    public void configureTables() {
        patientRepository.createTable();
        doctorRepository.createTable();
        treatmentRepository.createTable();
        appointmentRepository.createTable();
        paymentRepository.createTables();
    }

    //---------------------------------------------------------------PATIENT MENU RELATED FUNCTIONS-------------------------------------------------------------------
    public void viewPatients(){
        patientRepository.viewPatients();
    }
    public void searchPatientsByName(){
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter name: ");
        String name = reader.nextLine();
        patientRepository.searchPatientsByName(name);
    }
    public void viewPatientById(){
        patientRepository.viewPatients();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nPatient ID: ");
        int patientId;

        while(true)
        {
            try
            {
                patientId = Integer.parseInt(reader.nextLine());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("\nExpecting an integer value. Try again!");
                System.out.print("Patient ID: ");
            }
        }

        if (patientRepository.getPatientById(patientId) != null)
        {
            System.out.println(patientRepository.getPatientById(patientId).toString());
        }
        else
        {
            System.out.println("\nNo existing student with this id!");
        }
    }
    public void addPatient(){
        Scanner reader = new Scanner(System.in);

        System.out.println("\nPlease provide the following information for the new patient:");
        System.out.print("First name: ");
        String firstName = reader.nextLine();
        firstName = firstName.substring(0,1).toUpperCase() + firstName.substring(1).toLowerCase();

        System.out.print("Last name: ");
        String lastName = reader.nextLine();
        lastName = lastName.substring(0,1).toUpperCase() + lastName.substring(1).toLowerCase();

        String email;
        while(true)
        {
            System.out.print("Email: ");
            email = reader.nextLine().toLowerCase();

            if (!email.contains("@")) System.out.println("\nNot a valid email address! Try again!");
            else break;
        }

        System.out.println("Address: ");
        Address address = readAddress();

        System.out.print("Phone number: ");
        String phoneNumber = reader.nextLine();

        System.out.print("Birth date: ");
        LocalDateTime birthDate = readDateTime();

        // Calculate age
        int age = Period.between(LocalDate.from(birthDate), LocalDate.now()).getYears();

        String gender;
        while (true) {
            System.out.print("Gender (male/female): ");
            gender = reader.nextLine().toLowerCase();
            if (gender.equals("male") || gender.equals("female")) {
                gender = gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase();
                break;
            } else {
                System.out.println("\nInvalid gender! Please enter 'male' or 'female'.");
            }
        }

        System.out.print("Medical history: ");
        String medicalHistory = reader.nextLine();

        Patient newPatient = new Patient(firstName, lastName, email, address, phoneNumber, birthDate, age, gender, medicalHistory);
        patientRepository.addPatient(newPatient);
        patients.add(newPatient);
    }
    public static LocalDateTime readDateTime(){
        Scanner scanner = new Scanner(System.in);
        int year, month, day;

        while (true) {
            try {
                System.out.print("Enter birth year (yyyy): ");
                year = Integer.parseInt(scanner.nextLine());

                System.out.print("Enter birth month (MM): ");
                month = Integer.parseInt(scanner.nextLine());

                System.out.print("Enter birth day (dd): ");
                day = Integer.parseInt(scanner.nextLine());

                // Validate the date components
                LocalDate birthDate = LocalDate.of(year, month, day);
                return LocalDateTime.of(birthDate, LocalTime.MIDNIGHT);
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter numeric values for year, month, and day.");
            } catch (Exception e) {
                System.out.println("\nInvalid date. Please enter a valid date.");
            }
        }
    }
    public void updatePatient() {
        patientRepository.viewPatients();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter the ID of the patient you want to update: ");
        int patientId = Integer.parseInt(reader.nextLine());
        Patient patient = patientRepository.getPatientById(patientId);

        if (patient == null) {
            System.out.println("\nNo patient found with the given ID!");
            return;
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\nSelect the field to update:");
            System.out.println("1. First name");
            System.out.println("2. Last name");
            System.out.println("3. Email");
            System.out.println("4. Address");
            System.out.println("5. Phone number");
            System.out.println("6. Birth date");
            System.out.println("7. Age");
            System.out.println("8. Gender");
            System.out.println("9. Medical history");
            System.out.println("0. Exit");
            System.out.print("Your choice: ");
            int choice = Integer.parseInt(reader.nextLine());
            switch (choice) {
                case 1:
                    System.out.print("New first name: ");
                    String firstName = reader.nextLine();
                    firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
                    patient.setFirstName(firstName);
                    break;
                case 2:
                    System.out.print("New last name: ");
                    String lastName = reader.nextLine();
                    lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
                    patient.setLastName(lastName);
                    break;
                case 3:
                    String email;
                    while (true) {
                        System.out.print("New email: ");
                        email = reader.nextLine().toLowerCase();
                        if (!email.contains("@")) {
                            System.out.println("\nNot a valid email address! Try again!");
                        } else {
                            patient.setEmail(email);
                            break;
                        }
                    }
                    break;
                case 4:
                    System.out.println("New address:");
                    Address address = readAddress();
                    patient.setAddress(address);
                    break;
                case 5:
                    System.out.print("New phone number: ");
                    String phoneNumber = reader.nextLine();
                    patient.setPhoneNumber(phoneNumber);
                    break;
                case 6:
                    System.out.println("New birth date:");
                    LocalDateTime birthDate = readDateTime();
                    int age = Period.between(LocalDate.from(birthDate), LocalDate.now()).getYears();
                    patient.setBirthDate(birthDate);
                    patient.setAge(age);
                    break;
                case 7:
                    System.out.print("New age: ");
                    int newAge = Integer.parseInt(reader.nextLine());
                    patient.setAge(newAge);
                    break;
                case 8:
                    String gender;
                    while (true) {
                        System.out.print("New gender (male/female): ");
                        gender = reader.nextLine().toLowerCase();
                        if (gender.equals("male") || gender.equals("female")) {
                            gender = gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase();
                            patient.setGender(gender);
                            break;
                        } else {
                            System.out.println("\nInvalid gender! Please enter 'male' or 'female'.");
                        }
                    }
                    break;
                case 9:
                    System.out.print("New medical history: ");
                    String medicalHistory = reader.nextLine();
                    patient.setMedicalHistory(medicalHistory);
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("\nInvalid choice! Please try again.");
            }
        }
        patientRepository.updatePatient(patientId, patient);
    }
    public void deletePatientById(){
        patientRepository.viewPatients();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter the ID of the patient you want to delete: ");
        int patientId;

        while(true)
        {
            try
            {
                patientId = Integer.parseInt(reader.nextLine());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("\nExpecting an integer value. Try again!");
                System.out.print("Patient ID: ");
            }
        }

        if (patientRepository.patientExists(patientId)) {
            patientRepository.deletePatientById(patientId);
            //System.out.println("\nPatient deleted successfully!");

        } else {
            System.out.println("\nNo existing patient with this id!");
        }
    }
    public Address readAddress() {
        Scanner reader = new Scanner(System.in);


        System.out.print("Country: ");
        String country = reader.nextLine();
        country = country.substring(0,1).toUpperCase() + country.substring(1).toLowerCase();

        System.out.print("City: ");
        String city = reader.nextLine();
        city = city.substring(0,1).toUpperCase() + city.substring(1).toLowerCase();

        System.out.print("Street: ");
        String street = reader.nextLine();
        street = street.substring(0,1).toUpperCase() + street.substring(1).toLowerCase();

        System.out.print("Number: ");
        int number;

        while(true)
        {
            try
            {
                number = Integer.parseInt(reader.nextLine());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("\nExpecting an integer value. Try again!");
                System.out.print("Number: ");
            }
        }
        return new Address(country, city, street, number);
    }

    //---------------------------------------------------------------DOCTOR MENU RELATED FUNCTIONS-------------------------------------------------------------------

    public void viewDoctors(){
        doctorRepository.viewDoctors();
    }
    public void searchDoctorsBySpecialization(){
        String specialization = chooseTreatmentCategory().toString();
        doctorRepository.searchDoctorsBySpecialization(specialization);

    }
    public void searchDoctorsByName(){
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter name: ");
        String name = reader.nextLine();
        doctorRepository.searchDoctorsByName(name);
    }
    public TreatmentCategory chooseTreatmentCategory() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nChoose a specialization from the available categories: ");
        int index = 1;
        for (TreatmentCategory category : TreatmentCategory.values()) {
            System.out.println(index + ". " + category.name());
            index++;
        }
        System.out.print("Enter the number corresponding to the desired specialization: ");
        int choice = scanner.nextInt();
        while (choice < 1 || choice > TreatmentCategory.values().length) {
            System.out.println("Invalid choice. Please enter a number between 1 and " + TreatmentCategory.values().length + ".");
            System.out.print("Enter the number corresponding to the desired specialization: ");
            choice = scanner.nextInt();
        }
        return TreatmentCategory.values()[choice - 1];
    }
    public void viewDoctorById(){
        doctorRepository.viewDoctors();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nDoctor ID: ");
        int doctorId;

        while(true)
        {
            try
            {
                doctorId = Integer.parseInt(reader.nextLine());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("\nExpecting an integer value. Try again!");
                System.out.print("Doctor ID: ");
            }
        }

        if (doctorRepository.getDoctorById(doctorId) != null)
        {
            System.out.println(doctorRepository.getDoctorById(doctorId).toString());
        }
        else
        {
            System.out.println("\nNo existing doctor with this id!");
        }
    }
    public void updateDoctor(){
        doctorRepository.viewDoctors();
        Scanner reader = new Scanner(System.in);

        System.out.print("\nEnter the ID of the doctor you want to update: ");
        int doctorId = Integer.parseInt(reader.nextLine());
        Doctor doctor = doctorRepository.getDoctorById(doctorId);


        if (doctor == null) {
            System.out.println("\nNo doctor found with the given ID!");
            return;
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\nSelect the field to update:");
            System.out.println("1. First name");
            System.out.println("2. Last name");
            System.out.println("3. Email");
            System.out.println("4. Address");
            System.out.println("5. Phone Number");
            System.out.println("6. Specialization");
            System.out.println("0. Exit");
            System.out.print("Your choice: ");
            int choice = Integer.parseInt(reader.nextLine());
            switch (choice) {
                case 1:
                    System.out.print("New first name: ");
                    String firstName = reader.nextLine();
                    firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
                    doctor.setFirstName(firstName);
                    break;
                case 2:
                    System.out.print("New last name: ");
                    String lastName = reader.nextLine();
                    lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
                    doctor.setLastName(lastName);
                    break;
                case 3:
                    String email;
                    while (true) {
                        System.out.print("New email: ");
                        email = reader.nextLine().toLowerCase();
                        if (!email.contains("@")) {
                            System.out.println("\nNot a valid email address! Try again!");
                        } else {
                            doctor.setEmail(email);
                            break;
                        }
                    }
                    break;
                case 4:
                    System.out.println("New address:");
                    Address address = readAddress();
                    doctor.setAddress(address);
                    break;
                case 5:
                    System.out.print("New phone number: ");
                    String phoneNumber = reader.nextLine();
                    doctor.setPhoneNumber(phoneNumber);
                    break;
                case 6:

                    System.out.println("Available Specializations:");
                    int index = 1;
                    for (TreatmentCategory category : TreatmentCategory.values()) {
                        System.out.println(index + ". " + category.name());
                        index++;
                    }
                    System.out.print("Enter the number(s) of the specialization(s) you want to add (comma-separated): ");
                    String input = reader.nextLine();
                    String[] specialties = input.split(",");
                    Set<TreatmentCategory> newSpecializations = new HashSet<>();
                    for (String s : specialties) {
                        int specIndex = Integer.parseInt(s.trim());
                        if (specIndex >= 1 && specIndex <= TreatmentCategory.values().length) {
                            newSpecializations.add(TreatmentCategory.values()[specIndex - 1]);
                        } else {
                            System.out.println("Invalid specialization number: " + specIndex);
                        }
                    }
                    doctor.setSpecializations(newSpecializations);
                    //doctor.getSpecializations().addAll(newSpecializations);
                    break;

                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
        doctorRepository.updateDoctor(doctorId, doctor);
    }
    public void deleteDoctorById(){
        doctorRepository.viewDoctors();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter the ID of the doctor you want to delete: ");
        int doctorId;

        while(true)
        {
            try
            {
                doctorId = Integer.parseInt(reader.nextLine());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Expecting an integer value. Try again!");
                System.out.print("Doctor ID: ");
            }
        }

        if (doctorRepository.doctorExists(doctorId)) {
            doctorRepository.deleteDoctorById(doctorId);

        } else {
            System.out.println("No existing doctor with this ID!");
        }
    }
    public void addDoctor(){
        Scanner reader = new Scanner(System.in);

        System.out.println("\nPlease provide the following information for the new doctor:");
        System.out.print("First name: ");
        String firstName = reader.nextLine();
        firstName = firstName.substring(0,1).toUpperCase() + firstName.substring(1).toLowerCase();

        System.out.print("Last name: ");
        String lastName = reader.nextLine();
        lastName = lastName.substring(0,1).toUpperCase() + lastName.substring(1).toLowerCase();

        String email;
        while(true)
        {
            System.out.print("Email: ");
            email = reader.nextLine().toLowerCase();

            if (!email.contains("@")) System.out.println("Not a valid email address! Try again!");
            else break;
        }

        System.out.println("Address: ");
        Address address = readAddress();

        System.out.print("Phone number: ");
        String phoneNumber = reader.nextLine();

        System.out.println("Available Specializations:");
        int index = 1;
        for (TreatmentCategory category : TreatmentCategory.values()) {
            System.out.println(index + ". " + category.name());
            index++;
        }

        Set<TreatmentCategory> specializations = new HashSet<>();
        boolean validInput = false;
        while (!validInput) {
            System.out.print("Enter the number(s) of the specialization(s) for the doctor (comma-separated): ");
            String input = reader.nextLine();
            String[] specialties = input.split(",");
            for (String s : specialties) {
                try {
                    int specIndex = Integer.parseInt(s.trim());
                    if (specIndex >= 1 && specIndex <= TreatmentCategory.values().length) {
                        specializations.add(TreatmentCategory.values()[specIndex - 1]);
                    } else {
                        System.out.println("Invalid specialization number: " + specIndex);
                        specializations.clear();
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input: " + s);
                    specializations.clear();
                    break;
                }
            }
            if (!specializations.isEmpty()) {
                validInput = true;
            }
        }


        Doctor newDoctor = new Doctor(firstName, lastName, email, address, phoneNumber);
        newDoctor.setSpecializations(specializations);
        doctorRepository.addDoctor(newDoctor);
        doctors.add(newDoctor);
    }

    //---------------------------------------------------------------TREATMENT MENU RELATED FUNCTIONS-------------------------------------------------------------------

    public void viewTreatments(){
        treatmentRepository.viewTreatments();
    }
     public void searchTreatmentsByCategory(){
        String category = chooseTreatmentCategory().toString();
        treatmentRepository.searchTreatmentsByCategory(category);
     }
     public void searchTreatmentsByName(){
         Scanner reader = new Scanner(System.in);
         System.out.print("\nEnter name: ");
         String name = reader.nextLine();
         treatmentRepository.searchTreatmentsByName(name);
     }
     public void viewTreatmentById(){
         treatmentRepository.viewTreatments();
         Scanner reader = new Scanner(System.in);
         System.out.print("\nTreatment ID: ");
         int treatmentId;

         while(true)
         {
             try
             {
                 treatmentId = Integer.parseInt(reader.nextLine());
                 break;
             }
             catch (NumberFormatException e)
             {
                 System.out.println("\nExpecting an integer value. Try again!");
                 System.out.print("Treatment ID: ");
             }
         }

         if (treatmentRepository.getTreatmentById(treatmentId) != null)
         {
             System.out.println(treatmentRepository.getTreatmentById(treatmentId).toString());
         }
         else
         {
             System.out.println("\nNo existing treatment with this id!");
         }
     }
    public void updateTreatment() {
        treatmentRepository.viewTreatments();
        Scanner reader = new Scanner(System.in);

        System.out.print("\nEnter the ID of the treatment you want to update: ");
        int treatmentId = Integer.parseInt(reader.nextLine());
        Treatment treatment = treatmentRepository.getTreatmentById(treatmentId);

        if (treatment == null) {
            System.out.println("\nNo treatment found with the given ID!");
            return;
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\nSelect the field to update:");
            System.out.println("1. Name");
            System.out.println("2. Category");
            System.out.println("3. Price");
            System.out.println("0. Exit");
            System.out.print("Your choice: ");
            int choice = Integer.parseInt(reader.nextLine());
            switch (choice) {
                case 1:
                    System.out.print("New name: ");
                    String name = reader.nextLine();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                    treatment.setName(name);
                    break;
                case 2:
                    TreatmentCategory category = chooseTreatmentCategory();
                    treatment.setCategory(category);
                    break;
                case 3:
                    double price = -1;
                    while (price < 0) {
                        System.out.print("New price: ");
                        if (reader.hasNextDouble()) {
                            price = reader.nextDouble();
                            reader.nextLine();  // Consume the newline character
                            if (price < 0) {
                                System.out.println("Price must be a non-negative number. Please try again.");
                            }
                        } else {
                            System.out.println("Invalid input. Please enter a valid number for the price.");
                            reader.next();  // Consume the invalid input
                        }
                    }
                    treatment.setPrice(price);
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
        treatmentRepository.updateTreatment(treatmentId, treatment);
    }
    public void deleteTreatmentById(){
        treatmentRepository.viewTreatments();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter the ID of the treatment you want to delete: ");
        int treatmentId;

        while(true)
        {
            try
            {
                treatmentId = Integer.parseInt(reader.nextLine());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Expecting an integer value. Try again!");
                System.out.print("Treatment ID: ");
            }
        }

        if (treatmentRepository.treatmentExists(treatmentId)) {
            treatmentRepository.deleteTreatmentById(treatmentId);

        } else {
            System.out.println("No existing treatment with this ID!");
        }
    }
    public void addTreatment() {
        Scanner reader = new Scanner(System.in);
        System.out.println("\nPlease provide the following information for the new treatment to be made available at the clinic:");

        // Input and format the name
        System.out.print("Name: ");
        String name = reader.nextLine();
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

        // Choose the treatment category
        TreatmentCategory category = chooseTreatmentCategory();

        // Validate the price input
        double price = -1;
        while (price < 0) {
            System.out.print("Price: ");
            if (reader.hasNextDouble()) {
                price = reader.nextDouble();
                if (price < 0) {
                    System.out.println("Price must be a non-negative integer. Please try again.");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid integer for the price.");
                reader.next();
            }
        }
        Treatment newTreatment = new Treatment(category,name,price);
        treatmentRepository.addTreatment(newTreatment);
        treatments.add(newTreatment);
    }


    //---------------------------------------------------------------APPOINTMENT MENU RELATED FUNCTIONS-------------------------------------------------------------------

    public void viewAppointments(){
        appointmentRepository.viewAppointments();
    }
    public void viewCurrentAppointments() {
        // Get appointments for the adjusted date
        List<Appointment> appointments = appointmentRepository.getAllAppointments();

        LocalDate currentDate = LocalDate.now();

        // Check if it's Saturday or Sunday
        if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            // Increment the date to the next Monday
            currentDate = currentDate.plusDays(8 - currentDate.getDayOfWeek().getValue()); // 8 - dayOfWeek to get next Monday
        }

        // Filter appointments for the adjusted date
        List<Appointment> filteredAppointments = getAppointmentsForDate(appointments, currentDate);

        if (!filteredAppointments.isEmpty()) {
            // Display appointments
            System.out.println("Appointments for " + currentDate + ":");
            for (Appointment appointment : filteredAppointments) {
                System.out.println(appointment);
            }
        } else {
            // No appointments for the adjusted date
            System.out.println("No appointments scheduled for " + currentDate + ".");
        }
    }
    public void viewAppointmentById(){
        appointmentRepository.viewAppointments();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nAppointment ID: ");
        int appointmentId;

        while(true)
        {
            try
            {
                appointmentId = Integer.parseInt(reader.nextLine());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("\nExpecting an integer value. Try again!");
                System.out.print("Appointment ID: ");
            }
        }

        if (appointmentRepository.getAppointmentById(appointmentId) != null)
        {
            System.out.println(appointmentRepository.getAppointmentById(appointmentId).toString());
        }
        else
        {
            System.out.println("\nNo existing appointments with this id!");
        }
    }
    public void searchAppointmentsByPatient(){
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter name of the patient: ");
        String name = reader.nextLine();
        List<Appointment> patientAppointments = appointmentRepository.searchAppointmentsByPatient(name);
        System.out.println("Search results for appointments with patients with names '" + name.toUpperCase() + "':");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        for(Appointment app:patientAppointments){
            System.out.println(app);
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        }
    }
    public void searchAppointmentsByDoctor(){
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter name of the doctor: ");
        String name = reader.nextLine();
        List<Appointment> patientAppointments = appointmentRepository.searchAppointmentsByDoctor(name);
        System.out.println("Search results for appointments with doctors with names '" + name.toUpperCase() + "':");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        for(Appointment app:patientAppointments){
            System.out.println(app);
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        }
    }
    public void updateAppointment(){
        appointmentRepository.viewAppointments();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter the ID of the appointment you want to update: ");
        int appointmentId = Integer.parseInt(reader.nextLine());
        Appointment appointment = appointmentRepository.getAppointmentById(appointmentId);

        if (appointment == null) {
            System.out.println("\nNo appointment found with the given ID!");
            return;
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\nSelect the field to update:");
            System.out.println("1. Date");
            System.out.println("2. Patient");
            System.out.println("3. Doctor");
            System.out.println("4. Treatment");
            System.out.println("5. Status");
            System.out.println("0. Exit");
            System.out.print("Your choice: ");
            int choice = Integer.parseInt(reader.nextLine());
            switch (choice) {
                case 1:
                    LocalDateTime date = chooseAppointmentDate(appointment.getDoctor());
                    appointment.setDate(date);
                    break;
                case 2:
                    Patient patient = choosePatient();
                    appointment.setPatient(patient);
                    break;
                case 3:
                    Doctor doctor = chooseDoctor(appointment.getTreatment().getCategory());
                    LocalDateTime newdate = chooseAppointmentDate(doctor);
                    appointment.setDoctor(doctor);
                    appointment.setDate(newdate);
                    break;
                case 4:
                    TreatmentCategory category = chooseTreatmentCategory();
                    Treatment treatment = chooseTreatment(category);
                    Doctor newdoctor = chooseDoctor(category);
                    LocalDateTime newdate1 = chooseAppointmentDate(newdoctor);
                    appointment.setTreatment(treatment);
                    appointment.setDoctor(newdoctor);
                    appointment.setDate(newdate1);
                    break;
                case 5:
                    AppointmentStatus newStatus;
                    System.out.print("\nChoose the STATUS you want to set to the Appointment:\n");
                    AppointmentStatus[] statuses = AppointmentStatus.values();
                    for (int i = 0; i < statuses.length; i++) {
                        System.out.println((i + 1) + ". " + statuses[i]);
                    }

                    System.out.print("Please insert your option: ");
                    int opt;
                    try {
                        opt = Integer.parseInt(reader.nextLine());
                        if (opt < 1 || opt > statuses.length) {
                            System.out.println("Invalid choice. Please enter a number between 1 and " + statuses.length + ".");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        continue;
                    }
                    newStatus = statuses[opt - 1];
                    appointment.setStatus(newStatus);
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("\nInvalid choice! Please try again.");
            }
        }
        appointmentRepository.updateAppointment(appointmentId, appointment);
    }
    public void deleteAppointmentById(){
        appointmentRepository.viewAppointments();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter the ID of the appointment you want to delete: ");
        int appointmentId;

        while(true)
        {
            try
            {
                appointmentId = Integer.parseInt(reader.nextLine());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Expecting an integer value. Try again!");
                System.out.print("Appointment ID: ");
            }
        }

        if (appointmentRepository.appointmentExists(appointmentId)) {
            appointmentRepository.deleteAppointmentById(appointmentId);

        } else {
            System.out.println("No existing appointment with this ID!");
        }
    }
    private List<Appointment> getAppointmentsForDate(List<Appointment> appointments, LocalDate date) {
        List<Appointment> filteredAppointments = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getDate().toLocalDate().isEqual(date)) {
                filteredAppointments.add(appointment);
            }
        }
        // Sort the filtered appointments based on appointment time
        filteredAppointments.sort(Comparator.comparing(Appointment::getDate));

        return filteredAppointments;
    }
    public void addAppointment(){
        Scanner reader = new Scanner(System.in);
        System.out.println("\nPlease provide the following information for the new appointment:");
        Patient patient = choosePatient();
        TreatmentCategory category = chooseTreatmentCategory();
        Treatment treatment = chooseTreatment(category);
        Doctor doctor = chooseDoctor(category);
        LocalDateTime date = chooseAppointmentDate(doctor);
        Appointment appointment = new Appointment(date,patient,treatment,doctor, AppointmentStatus.SCHEDULED);
        appointmentRepository.addAppointment(appointment);
    }
    public Patient choosePatient(){
        Scanner reader = new Scanner(System.in);
        Patient patient;
        System.out.print("Is the patient registered? (yes/no): ");
        String response = reader.nextLine();
        switch (response.toLowerCase()) {
            case "yes":
                patientRepository.viewPatients();
                while(true){
                    System.out.print("Enter the ID of the patient:");
                    int patientID = reader.nextInt();
                    reader.nextLine();
                    patient = patientRepository.getPatientById(patientID);
                    if (patient == null) {
                        System.out.println("Patient not found. Please try again.");
                    } else {
                        break;
                    }
                }
                break;
            case "no":
                System.out.println("\nPlease provide the following information for the new patient:");
                System.out.print("First name: ");
                String firstName = reader.nextLine();
                firstName = firstName.substring(0,1).toUpperCase() + firstName.substring(1).toLowerCase();

                System.out.print("Last name: ");
                String lastName = reader.nextLine();
                lastName = lastName.substring(0,1).toUpperCase() + lastName.substring(1).toLowerCase();

                String email;
                while(true)
                {
                    System.out.print("Email: ");
                    email = reader.nextLine().toLowerCase();

                    if (!email.contains("@")) System.out.println("\nNot a valid email address! Try again!");
                    else break;
                }

                System.out.println("Address: ");
                Address address = readAddress();

                System.out.print("Phone number: ");
                String phoneNumber = reader.nextLine();

                System.out.print("Birth date: ");
                LocalDateTime birthDate = readDateTime();

                // Calculate age
                int age = Period.between(LocalDate.from(birthDate), LocalDate.now()).getYears();

                String gender;
                while (true) {
                    System.out.print("Gender (male/female): ");
                    gender = reader.nextLine().toLowerCase();
                    if (gender.equals("male") || gender.equals("female")) {
                        gender = gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase();
                        break;
                    } else {
                        System.out.println("\nInvalid gender! Please enter 'male' or 'female'.");
                    }
                }

                System.out.print("Medical history: ");
                String medicalHistory = reader.nextLine();

                patient = new Patient(firstName, lastName, email, address, phoneNumber, birthDate, age, gender, medicalHistory);
                patientRepository.addPatient(patient);
                patients.add(patient);

//        try
//        {
//            auditService.logAction("add patient");
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
                break;
            default:
                System.out.println("Invalid response.");
                return choosePatient();
        }
        return patient;
    }
    public Treatment chooseTreatment(TreatmentCategory category) {
        List<Treatment> treatments = treatmentRepository.getAllTreatments();
        List<Treatment> categoryTreatments = new ArrayList<>();

        // Iterate through treatments and add those with the specified category to a new list
        for (Treatment treatment : treatments) {
            if (treatment.getCategory().equals(category)) {
                categoryTreatments.add(treatment);
            }
        }

        // Check if there are no treatments in the specified category
        if (categoryTreatments.isEmpty()) {
            System.out.println("\nNo treatments available for the specified category.");
            return null; // If no treatments are available
        }

        // Display the list of treatments available for the specified category
        System.out.println("\nAvailable treatments for category " + category + ":");
        for (int i = 0; i < categoryTreatments.size(); i++) {
            System.out.println((i + 1) + ". " + categoryTreatments.get(i).getName());
        }

        // Wait for the user to select a valid number
        int choice = -1;
        Scanner scanner = new Scanner(System.in);
        while (choice < 1 || choice > categoryTreatments.size()) {
            System.out.print("Choose the number corresponding to the desired treatment: ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                if (choice < 1 || choice > categoryTreatments.size()) {
                    System.out.println("Please enter a valid number.");
                }
            } else {
                System.out.println("Please enter a valid number.");
                scanner.next();
            }
        }

        return categoryTreatments.get(choice - 1);
    }
    public Doctor chooseDoctor(TreatmentCategory category) {
        List<Doctor> doctors = doctorRepository.getAllDoctors();
        List<Doctor> categoryDoctors = new ArrayList<>();

        // Iterate through doctors and add those with the specified category to a new list
        for (Doctor doctor : doctors) {
            if (doctor.getSpecializations().contains(category)) {
                categoryDoctors.add(doctor);
            }
        }

        // Check if there are no doctors in the specified category
        if (categoryDoctors.isEmpty()) {
            System.out.println("\nNo doctors available for the specified category.");
            return null; // If no doctors are available
        }

        // Display the list of doctors available for the specified category
        System.out.println("\nAvailable doctors for category " + category + ":");
        for (int i = 0; i < categoryDoctors.size(); i++) {
            Doctor doctor = categoryDoctors.get(i);
            System.out.println((i + 1) + ". Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
        }

        // Wait for the user to select a valid number
        int choice = -1;
        Scanner scanner = new Scanner(System.in);
        while (choice < 1 || choice > categoryDoctors.size()) {
            System.out.print("Choose the number corresponding to the desired doctor: ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                if (choice < 1 || choice > categoryDoctors.size()) {
                    System.out.println("Please enter a valid number.");
                }
            } else {
                System.out.println("Please enter a valid number.");
                scanner.next(); // Consume the invalid input
            }
        }

        // Return the doctor selected by the user (the index of the doctor is 1 less than the number chosen by the user)
        return categoryDoctors.get(choice - 1);
    }
    public LocalDateTime chooseAppointmentDate(Doctor doctor) {
    List<Appointment> appointments = appointmentRepository.getAllAppointments();
    List<Appointment> doctorAppointments = new ArrayList<>();

    for (Appointment appointment : appointments) {
        if (appointment.getDoctor().equals(doctor)) {
            doctorAppointments.add(appointment);
        }
    }

    Scanner scanner = new Scanner(System.in);
    LocalDate chosenDate = null;
    LocalDateTime chosenDateTime = null;
    boolean isValid = false;

    // Buclă pentru alegerea unei date valide
    while (!isValid) {
        System.out.print("Enter appointment date (YYYY-MM-DD): ");
        String inputDate = scanner.nextLine();

        try {
            chosenDate = LocalDate.parse(inputDate);

            // Pasul 2: Verifică dacă ziua nu cade într-o sâmbătă sau duminică
            if (chosenDate.getDayOfWeek().getValue() >= 6) {
                System.out.println("Selected day falls on a weekend. Please choose a weekday.");
                continue;
            }

            // Pasul 3: Verifică intervalele orare libere în ziua respectivă
            List<LocalTime> availableTimes = getAvailableTimes(doctorAppointments, chosenDate);

            if (availableTimes.isEmpty()) {
                System.out.println("No available times on this day. Please choose another day.");
                continue;
            }

            System.out.println("Available times: ");
            for (int i = 0; i < availableTimes.size(); i++) {
                System.out.println((i + 1) + ". " + availableTimes.get(i));
            }

            boolean validTimeChosen = false;
            while (!validTimeChosen) {
                System.out.print("Choose a time slot (number): ");
                int timeChoice = scanner.nextInt();
                scanner.nextLine();  // Clear the newline

                if (timeChoice < 1 || timeChoice > availableTimes.size()) {
                    System.out.println("Invalid choice. Please choose a valid time slot.");
                } else {
                    LocalTime chosenTime = availableTimes.get(timeChoice - 1);
                    chosenDateTime = LocalDateTime.of(chosenDate, chosenTime);
                    validTimeChosen = true;
                }
            }
            isValid = true;

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please try again.");
        }
    }

    return chosenDateTime;
}
    private List<LocalTime> getAvailableTimes(List<Appointment> doctorAppointments, LocalDate chosenDate) {
        List<LocalTime> availableTimes = new ArrayList<>();

        LocalTime startWorkingTime = LocalTime.of(8, 0); // 08:00
        LocalTime endWorkingTime = LocalTime.of(16, 0);  // 16:00

        // Add all possible times in the working hours
        for (LocalTime time = startWorkingTime; time.isBefore(endWorkingTime); time = time.plusHours(1)) {
            availableTimes.add(time);
        }

        // Check each appointment for the chosen date and remove corresponding time slots
        for (Appointment appointment : doctorAppointments) {
            LocalDateTime appointmentDateTime = appointment.getDate();
            LocalDate appointmentDate = appointmentDateTime.toLocalDate();
            LocalTime appointmentTime = appointmentDateTime.toLocalTime();

            if (appointmentDate.equals(chosenDate)) {
                availableTimes.remove(appointmentTime);
            }
        }

        return availableTimes;
    }



    //---------------------------------------------------------------PAYMENT MENU RELATED FUNCTIONS-------------------------------------------------------------------
    public void viewPayments(){
        paymentRepository.viewPayments();
    }
    public void searchPaymentsByPatient(){
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter name of the patient: ");
        String name = reader.nextLine();
        List<Payment> patientPayments = paymentRepository.searchPaymentsByPatient(name);
        System.out.println("\nSearch results for payments with patients with names '" + name.toUpperCase() + "':");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        for(Payment p:patientPayments){
            System.out.println(p);
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        }
    }
    public void viewPaymentById(){
        paymentRepository.viewPayments();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nPayment ID: ");
        int paymentId;

        while(true)
        {
            try
            {
                paymentId = Integer.parseInt(reader.nextLine());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("\nExpecting an integer value. Try again!");
                System.out.print("Payment ID: ");
            }
        }

        if (paymentRepository.getPaymentById(paymentId) != null)
        {
            System.out.println(paymentRepository.getPaymentById(paymentId).toString());
        }
        else
        {
            System.out.println("\nNo existing payment with this id!");
        }
    }
    public void updatePayment(){
        paymentRepository.viewPayments();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter the ID of the payment you want to update: ");
        int paymentId = Integer.parseInt(reader.nextLine());
        Payment payment = paymentRepository.getPaymentById(paymentId);

        if (payment == null) {
            System.out.println("\nNo payment found with the given ID!");
            return;
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\nSelect the field to update:");
            System.out.println("1. Date");
            System.out.println("2. Patient");
            System.out.println("3. Treatments");
            System.out.println("0. Exit");
            System.out.print("Your choice: ");
            int choice = Integer.parseInt(reader.nextLine());
            switch (choice) {
                case 1:
                    LocalDateTime date = chooseDate();
                    payment.setPaymentDate(date);
                    break;
                case 2:
                    Patient patient = choosePatient();
                    payment.setPatient(patient);
                    break;
                case 3:
                    List<Treatment> treatments = chooseTreatments();
                    double totalAmount = 0.0;

                    for (Treatment tr : treatments) {
                        totalAmount += tr.getPrice();
                    }
                    payment.setTreatments(treatments);
                    payment.setTotalAmount(totalAmount);
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("\nInvalid choice! Please try again.");
            }
        }
        paymentRepository.updatePayment(paymentId, payment);
    }
    public void deletePaymentById(){
        paymentRepository.viewPayments();
        Scanner reader = new Scanner(System.in);
        System.out.print("\nEnter the ID of the payment you want to delete: ");
        int paymentId;

        while(true)
        {
            try
            {
                paymentId = Integer.parseInt(reader.nextLine());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Expecting an integer value. Try again!");
                System.out.print("Payment ID: ");
            }
        }

        if (paymentRepository.paymentExists(paymentId)) {
            paymentRepository.deletePaymentById(paymentId);

        } else {
            System.out.println("\nNo existing payment with this ID!");
        }
    }
    public void addPayment() {
        Patient patient = choosePatient();
        List<Treatment> treatments = chooseTreatments();
        LocalDateTime date = chooseDate();
        double totalAmount = 0.0;

        for (Treatment tr : treatments) {
            totalAmount += tr.getPrice();
        }

        Payment payment = new Payment(date, totalAmount, patient);
        payment.setTreatments(treatments);
        paymentRepository.addPayment(payment);
    }
    public List<Treatment> chooseTreatments() {
        Scanner scanner = new Scanner(System.in);
        List<Treatment> selectedTreatments = new ArrayList<>();

        treatmentRepository.viewTreatments();
        while (true) {
            System.out.print("\nEnter the ID of the treatment you want to add (or type 'done' to finish): ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("done")) {
                break;
            }

            try {
                int treatmentId = Integer.parseInt(input);
                Treatment treatment = treatmentRepository.getTreatmentById(treatmentId);

                if (treatment != null) {
                    if (!selectedTreatments.contains(treatment)) {
                        selectedTreatments.add(treatment);
                        System.out.println("Treatment added: " + treatment.getName());
                    }
                } else {
                    System.out.println("No treatment found with ID: " + treatmentId);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid treatment ID or 'done' to finish.");
            }
        }

        return selectedTreatments;
    }
    public LocalDateTime chooseDate() {
        Scanner scanner = new Scanner(System.in);
        LocalDateTime chosenDateTime = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (true) {
            try {
                System.out.print("\nEnter date (YYYY-MM-DD): ");
                String dateInput = scanner.nextLine();

                LocalDate chosenDate = LocalDate.parse(dateInput, formatter);
                DayOfWeek dayOfWeek = chosenDate.getDayOfWeek();

                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    System.out.println("The chosen date falls on a weekend. Please choose a weekday.");
                    continue;
                }

                LocalTime currentTime = LocalTime.now();
                chosenDateTime = LocalDateTime.of(chosenDate, currentTime);
                break;

            } catch (DateTimeParseException e) {
                System.out.println("Invalid input. Please enter a valid date in the format YYYY-MM-DD.");
            }
        }

        return chosenDateTime;
    }
    public void closeConnection()
    {
        DatabaseConfiguration.closeDatabaseConnection();
    }

}
