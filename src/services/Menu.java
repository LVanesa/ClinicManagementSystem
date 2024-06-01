package services;

import java.util.Scanner;

public class Menu {
    private static Menu instance;
    private static final Service service = Service.getInstance();
    private Menu() {
        service.configureTables();
    }
    static {
        try {
            instance = new Menu();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in creating Menu singleton instance");
        }
    }
    public static Menu getInstance() {
        if (instance == null) {
            instance = new Menu();
        }
        return instance;
    }
    private void welcomeMessage(){
        System.out.println();
        System.out.println();
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.println("                  Welcome to KINETIC MOVEMENT MEDICAL CENTER Management System Application");
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.println();
        System.out.println();
    }
    private void mainMenuOptions(){
        System.out.println("\nChoose a MENU:");
        System.out.println("    1. PATIENTS MENU");
        System.out.println("    2. DOCTORS MENU");
        System.out.println("    3. TREATMENTS MENU");
        System.out.println("    4. APPOINTMENTS MENU");
        System.out.println("    5. PAYMENTS MENU");
        System.out.println("    0. Exit.");
    }
    private void patientsMenu(){
        System.out.println("\nChoose an option:");
        System.out.println("    1. View all Patients");
        System.out.println("    2. Search Patients by NAME");
        System.out.println("    3. View a Patient");
        System.out.println("    4. Edit a Patient");
        System.out.println("    5. Delete a Patient");
        System.out.println("    6. Add a new Patient");
        System.out.println("    0. Back to Main Menu.");
    }
    private void doctorsMenu(){
        System.out.println("\nChoose an option:");
        System.out.println("    1. View all Doctors");
        System.out.println("    2. Search Doctors by SPECIALIZATION");
        System.out.println("    3. Search Doctors by NAME");
        System.out.println("    4. View a Doctor");
        System.out.println("    5. Edit a Doctor");
        System.out.println("    6. Delete a Doctor");
        System.out.println("    7. Add a new Doctor");
        System.out.println("    0. Back to Main Menu.");
    }

    private void treatmentsMenu(){
        System.out.println("\nChoose an option:");
        System.out.println("    1. View all Treatments");
        System.out.println("    2. Search Treatments from CATEGORY");
        System.out.println("    3. Search Treatments by NAME");
        System.out.println("    4. View a Treatment");
        System.out.println("    5. Edit a Treatment");
        System.out.println("    6. Delete a Treatment");
        System.out.println("    7. Add a new Treatment");
        System.out.println("    0. Back to Main Menu.");
    }
    private void appointmentsMenu(){
        System.out.println("\nChoose an option:");
        System.out.println("    1. View all Appointments");
        System.out.println("    2. View Current Appointments"); //le afiseaza doar pe cele programate in viitor
        System.out.println("    3. View an Appointment");
        System.out.println("    4. Search Appointments by Patient");
        System.out.println("    5. Search Appointments by Doctor");
        System.out.println("    6. Edit an Appointment");
        System.out.println("    7. Delete an Appointment");
        System.out.println("    8. Add an Appointment");
        System.out.println("    0. Back to Main Menu.");
    }

    private void paymentsMenu(){
        System.out.println("\nChoose an option:");
        System.out.println("    1. View all Payments");
        System.out.println("    2. Search Payment by PATIENT NAME");
        System.out.println("    3. View a Payment");
        System.out.println("    4. Edit a Payment");
        System.out.println("    5. Delete a Payment");
        System.out.println("    6. Add a new Payment");
        System.out.println("    0. Back to Main Menu.");
    }


    public void runMenu(){
        Service service = Service.getInstance();
        Scanner reader = new Scanner(System.in);
        int option;
        welcomeMessage();
        do {
            mainMenuOptions();
            System.out.print("Please insert your option: ");
            option = reader.nextInt();
            reader.nextLine();
            System.out.println("----------------------------------------------------------------------------------------------");
            switch (option) {
                case 1:
                    int subOption1;
                    do{
                        patientsMenu();
                        System.out.print("Please insert your option: ");
                        subOption1 = reader.nextInt();
                        reader.nextLine();
                        System.out.println("----------------------------------------------------------------------------------------------");
                        switch (subOption1){
                            case 1:
                                service.viewPatients();
                                break;
                            case 2:
                                service.searchPatientsByName();
                                break;
                            case 3:
                                service.viewPatientById();
                                break;
                            case 4:
                                service.updatePatient();
                                break;
                            case 5:
                                service.deletePatientById();
                                break;
                            case 6:
                                service.addPatient();
                                break;
                            case 0:
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                                break;
                        }
                    }while(subOption1 != 0);
                    break;

                case 2:
                    int subOption2;
                    do{
                        doctorsMenu();
                        System.out.print("Please insert your option: ");
                        subOption2 = reader.nextInt();
                        reader.nextLine();
                        System.out.println("----------------------------------------------------------------------------------------------");

                        switch (subOption2){
                            case 1:
                                service.viewDoctors();
                                break;
                            case 2:
                                service.searchDoctorsBySpecialization();
                                break;
                            case 3:
                                service.searchDoctorsByName();
                                break;
                            case 4:
                                service.viewDoctorById();
                                break;
                            case 5:
                                service.updateDoctor();
                                break;
                            case 6:
                                service.deleteDoctorById();
                                break;
                            case 7:
                                service.addDoctor();
                                break;
                            case 0:
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                                break;
                        }
                    }while(subOption2 != 0);
                    break;
                case 3:
                    int subOption3;
                    do{
                        treatmentsMenu();
                        System.out.print("Please insert your option: ");
                        subOption3 = reader.nextInt();
                        reader.nextLine();
                        System.out.println("----------------------------------------------------------------------------------------------");
                        switch (subOption3){
                            case 1:
                                service.viewTreatments();
                                break;
                            case 2:
                                service.searchTreatmentsByCategory();
                                break;
                            case 3:
                                service.searchTreatmentsByName();
                                break;
                            case 4:
                                service.viewTreatmentById();
                                break;
                            case 5:
                                service.updateTreatment();
                                break;
                            case 6:
                                service.deleteTreatmentById();
                                break;
                            case 7:
                                service.addTreatment();
                                break;
                            case 0:
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                                break;
                        }
                    }while(subOption3 != 0);
                    break;
                case 4:
                    int subOption4;
                    do{
                        appointmentsMenu();
                        System.out.print("Please insert your option: ");
                        subOption4 = reader.nextInt();
                        reader.nextLine();
                        System.out.println("----------------------------------------------------------------------------------------------");
                        switch (subOption4){
                            case 1:
                                service.viewAppointments();
                                break;
                            case 2:
                                service.viewCurrentAppointments();
                                break;
                            case 3:
                                service.viewAppointmentById();
                                break;
                            case 4:
                                service.searchAppointmentsByPatient();
                                break;
                            case 5:
                                service.searchAppointmentsByDoctor();
                                break;
                            case 6:
                                service.updateAppointment();
                                break;
                            case 7:
                                service.deleteAppointmentById();
                                break;
                            case 8:
                                service.addAppointment();
                                break;
                            case 0:
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                                break;
                        }
                    }while(subOption4 != 0);
                    break;
                case 5:
                    int subOption5;
                    do{
                        paymentsMenu();
                        System.out.print("Please insert your option: ");
                        subOption5 = reader.nextInt();
                        reader.nextLine();
                        System.out.println("----------------------------------------------------------------------------------------------");
                        switch (subOption5){
                            case 1:
                                service.viewPayments();
                                break;
                            case 2:
                                service.searchPaymentsByPatient();
                                break;
                            case 3:
                                service.viewPaymentById();
                                break;
                            case 4:
                                service.updatePayment();
                                break;
                            case 5:
                                service.deletePaymentById();
                                break;
                            case 6:
                                service.addPayment();
                                break;
                            case 0:
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                                break;
                        }
                    }while(subOption5 != 0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        } while (option != 0);
        System.out.println("You left the app. Goodbye!");
        reader.close();
    }
}
