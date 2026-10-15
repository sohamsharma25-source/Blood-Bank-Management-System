package bloodbank;

import bloodbank.exception.*;
import bloodbank.model.*;
import bloodbank.service.BloodBankSystem;
import bloodbank.thread.LowStockMonitor;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final BloodBankSystem app = new BloodBankSystem();
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    public static void main(String[] args) {
        System.out.println("=== BLOOD BANK MANAGEMENT SYSTEM ===");
        System.out.println("Demo admin credentials: admin / admin123 (change for any real deployment)");

        boolean run = true;
        while (run) {
            System.out.println("\n1. Hospital\n2. Admin\n0. Exit");
            int c = readInt("Choose: ");
            try {
                switch (c) {
                    case 1:
                        hospitalEntry();
                        break;
                    case 2:
                        adminLogin();
                        break;
                    case 0:
                        run = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        sc.close();
        System.out.println("Goodbye.");
    }

    private static void hospitalEntry() {
        System.out.println("\n1. Register hospital\n2. Hospital login\n0. Back");
        int c = readInt("Choose: ");
        if (c == 1) {
            HospitalUser h = app.registerHospital(
                    read("Hospital name: "),
                    read("Contact person: "),
                    read("Phone: "),
                    read("Address: "),
                    read("Username: "),
                    read("Password: "));
            System.out.println("Registration submitted. Hospital ID: " + h.getId()
                    + " | Status: PENDING. Ask admin to approve it.");
        } else if (c == 2) {
            String u = read("Username: ");
            String p = read("Password: ");
            HospitalUser h = app.hospitalLogin(u, p);
            if (h == null) {
                System.out.println("Invalid username or password.");
            } else {
                hospitalMenu(h);
            }
        } else if (c != 0) {
            System.out.println("Invalid choice.");
        }
    }

    private static void hospitalMenu(HospitalUser h) {
        boolean run = true;
        while (run) {
            System.out.println("\n--- HOSPITAL DASHBOARD: " + h.getName()
                    + " ---\n1. View profile\n2. View hospital inventory\n3. Request blood\n4. View my requests\n5. View issue records\n0. Logout");
            int c = readInt("Choose: ");
            try {
                switch (c) {
                    case 1:
                        System.out.println(h.displayName() + " | ID=" + h.getId() + " | status=" + h.getStatus()
                                + " | contact=" + h.getContactPerson() + " | phone=" + h.getPhone() + " | address="
                                + h.getAddress());
                        break;
                    case 2:
                        app.printHospitalInventory(h.getId());
                        break;
                    case 3:
                        BloodGroup g = chooseGroup();
                        int n = readInt("Units required: ");
                        int pv = readInt("Priority (1 Normal, 2 Emergency): ");
                        RequestPriority pr = pv == 2 ? RequestPriority.EMERGENCY : RequestPriority.NORMAL;
                        BloodRequest r = app.requestBlood(h.getId(), g, n, pr);
                        System.out.println("Request submitted: " + r.getId() + " | PENDING");
                        break;
                    case 4:
                        app.printHospitalRequests(h.getId());
                        break;
                    case 5:
                        app.printHospitalIssues(h.getId());
                        break;
                    case 0:
                        run = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void adminLogin() {
        String u = read("Admin username: ");
        String p = read("Admin password: ");
        if (!ADMIN_USER.equals(u) || !ADMIN_PASS.equals(p)) {
            System.out.println("Invalid admin credentials.");
            return;
        }
        Admin admin = new Admin("ADMIN-1", "System Administrator", u, p);
        System.out.println("Welcome, " + admin.displayName());
        adminMenu();
    }

    private static void adminMenu() {
        boolean run = true;
        while (run) {
            System.out.println(
                    "\n--- ADMIN DASHBOARD ---\n1. View hospitals\n2. Approve/reject hospital\n3. Register donor\n4. View donors\n5. Record donation\n6. View donations\n7. Accept/reject donation\n8. View inventory\n9. Set low-stock threshold\n10. View requests\n11. Approve/reject request\n12. Issue blood\n13. View issue records\n14. Summary report\n15. Set donation interval\n0. Logout");
            int c = readInt("Choose: ");
            try {
                switch (c) {
                    case 1:
                        app.printHospitals();
                        break;
                    case 2:
                        app.printHospitals();
                        String hid = read("Hospital ID: ");
                        int hd = readInt("1 Approve, 2 Reject: ");
                        if (hd != 1 && hd != 2) {
                            throw new IllegalArgumentException("Choose 1 or 2");
                        }
                        app.decideHospital(hid, hd == 1 ? HospitalStatus.APPROVED : HospitalStatus.REJECTED);
                        System.out.println("Hospital status updated.");
                        break;
                    case 3:
                        Donor d = app.addDonor(read("Donor name: "), readInt("Age: "), read("Phone: "), chooseGroup());
                        System.out.println("Donor registered. ID: " + d.getId());
                        break;
                    case 4:
                        app.printDonors();
                        break;
                    case 5:
                        app.printDonors();
                        String did = read("Donor ID: ");
                        int qty = readInt("Donation units (1-5): ");
                        String dateStr = read("Donation date (YYYY-MM-DD or Enter for today): ");
                        LocalDate donDate = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);
                        Donation don = app.recordDonation(did, qty, donDate);
                        System.out.println("Donation recorded: " + don.getId() + " | PENDING screening");
                        break;
                    case 6:
                        app.printDonations();
                        break;
                    case 7:
                        app.printDonations();
                        String dn = read("Donation ID: ");
                        int dec = readInt("1 Accept, 2 Reject: ");
                        if (dec != 1 && dec != 2) {
                            throw new IllegalArgumentException("Choose 1 or 2");
                        }
                        app.decideDonation(dn, dec == 1 ? DonationStatus.ACCEPTED : DonationStatus.REJECTED);
                        System.out.println("Donation decision saved.");
                        break;
                    case 8:
                        app.printInventory();
                        break;
                    case 9:
                        BloodGroup tg = chooseGroup();
                        System.out.println("Current threshold for " + tg.label() + ": " + app.threshold(tg));
                        int nth = readInt("New threshold: ");
                        app.setThreshold(tg, nth);
                        System.out.println("Threshold for " + tg.label() + " updated to " + nth + ".");
                        break;
                    case 10:
                        app.printRequests();
                        break;
                    case 11:
                        app.printRequests();
                        if (app.requestCount() == 0) {
                            break;
                        }
                        if (app.pendingRequestCount() == 0) {
                            System.out.println("No pending requests to approve or reject.");
                            break;
                        }
                        String rid = read("Request ID: ");
                        int rd = readInt("1 Approve, 2 Reject: ");
                        if (rd != 1 && rd != 2) {
                            throw new IllegalArgumentException("Choose 1 or 2");
                        }
                        app.decideRequest(rid, rd == 1, read("Decision note/reason: "));
                        System.out.println("Request reviewed.");
                        break;
                    case 12:
                        app.printRequests();
                        if (app.requestCount() == 0) {
                            break;
                        }
                        String ir = read("Approved request ID: ");
                        int iq = readInt("Units to issue: ");
                        app.issueBlood(ir, iq);
                        System.out.println("Issue recorded; inventory updated.");
                        break;
                    case 13:
                        app.printIssues();
                        break;
                    case 14:
                        app.printReport();
                        break;
                    case 15:
                        System.out.println("Current donation interval: " + app.getDonationIntervalDays() + " day(s).");
                        int interval = readInt("Enter new donation interval in days (standard: 90): ");
                        app.setDonationIntervalDays(interval);
                        System.out.println("Donation interval updated to " + interval + " day(s).");
                        break;
                    case 0:
                        run = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (DonorIneligibleException e) {
                System.out.println("DEFERRAL NOTICE: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static BloodGroup chooseGroup() {
        System.out.println("Blood groups:");
        BloodGroup[] gs = BloodGroup.values();
        for (int i = 0; i < gs.length; i++) {
            System.out.println((i + 1) + ". " + gs[i].label());
        }
        return BloodGroup.fromNumber(readInt("Choose blood group (1-8): "));
    }

    private static String read(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }
}