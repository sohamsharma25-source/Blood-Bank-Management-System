package bloodbank.service;

import bloodbank.exception.*;
import bloodbank.model.*;
import bloodbank.persistence.FileManager;
import bloodbank.util.IdGenerator;
import java.time.LocalDate;
import java.util.*;

/** Core business logic. Arrays are the primary record collections. */
public class BloodBankSystem {
    private final HospitalUser[] hospitals = new HospitalUser[50];
    private final Donor[] donors = new Donor[100];
    private final Donation[] donations = new Donation[200];
    private final BloodUnit[] units = new BloodUnit[300];
    private final BloodRequest[] requests = new BloodRequest[100];
    private final BloodIssue[] issues = new BloodIssue[300];

    private int hc;
    private int dc;
    private int doc;
    private int uc;
    private int rc;
    private int ic;
    private final FileManager files = new FileManager();

    private final IdGenerator hospitalIds = new IdGenerator("H", 1);
    private final IdGenerator donorIds = new IdGenerator("D", 1);
    private final IdGenerator donationIds = new IdGenerator("DN", 1);
    private final IdGenerator unitIds = new IdGenerator("U", 1);
    private final IdGenerator requestIds = new IdGenerator("R", 1);
    private final IdGenerator issueIds = new IdGenerator("I", 1);

    private final int[] thresholds = new int[BloodGroup.values().length];

    public BloodBankSystem() {
        Arrays.fill(thresholds, 3);
        load();
    }

    public synchronized HospitalUser registerHospital(String name, String contact, String phone, String address, String username, String password) {
        if (findHospitalByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        HospitalUser h = new HospitalUser(hospitalIds.next(), name, contact, phone, address, username, password, HospitalStatus.PENDING);
        addHospital(h);
        saveHospitals();
        return h;
    }

    public synchronized void addHospital(HospitalUser h) {
        if (hc == hospitals.length) {
            throw new IllegalStateException("Hospital capacity reached");
        }
        if (findHospital(h.getId()) != null) {
            throw new IllegalArgumentException("Duplicate hospital ID");
        }
        hospitals[hc++] = h;
    }

    public synchronized HospitalUser findHospital(String id) {
        for (int i = 0; i < hc; i++) {
            if (hospitals[i].getId().equalsIgnoreCase(id)) {
                return hospitals[i];
            }
        }
        return null;
    }

    public synchronized HospitalUser findHospitalByUsername(String u) {
        for (int i = 0; i < hc; i++) {
            if (hospitals[i].getUsername().equalsIgnoreCase(u)) {
                return hospitals[i];
            }
        }
        return null;
    }

    public synchronized HospitalUser hospitalLogin(String username, String password) {
        HospitalUser h = findHospitalByUsername(username);
        if (h == null || !h.getPassword().equals(password)) {
            return null;
        }
        if (h.getStatus() != HospitalStatus.APPROVED) {
            throw new IllegalArgumentException("Hospital is not approved. Status: " + h.getStatus());
        }
        return h;
    }

    public synchronized void decideHospital(String id, HospitalStatus status) {
        HospitalUser h = findHospital(id);
        if (h == null) {
            throw new IllegalArgumentException("Hospital not found");
        }
        if (status == HospitalStatus.PENDING) {
            throw new IllegalArgumentException("Cannot decide as pending");
        }
        h.setStatus(status);
        saveHospitals();
    }

    public synchronized Donor addDonor(String name, int age, String phone, BloodGroup group) {
        if (dc == donors.length) {
            throw new IllegalStateException("Donor capacity reached");
        }
        Donor d = new Donor(donorIds.next(), name, age, phone, group);
        donors[dc++] = d;
        saveDonors();
        return d;
    }

    public synchronized Donor findDonor(String id) {
        for (int i = 0; i < dc; i++) {
            if (donors[i].getId().equalsIgnoreCase(id)) {
                return donors[i];
            }
        }
        return null;
    }

    public synchronized Donation recordDonation(String donorId, int units, LocalDate date) {
        Donor d = findDonor(donorId);
        if (d == null) {
            throw new IllegalArgumentException("Donor not found");
        }
        if (doc == donations.length) {
            throw new IllegalStateException("Donation capacity reached");
        }
        Donation x = new Donation(donationIds.next(), donorId, d.getBloodGroup(), units, date, DonationStatus.PENDING, false);
        donations[doc++] = x;
        saveDonations();
        return x;
    }

    public synchronized void decideDonation(String id, DonationStatus status) throws InvalidOperationException {
        Donation d = findDonation(id);
        if (d == null) {
            throw new IllegalArgumentException("Donation not found");
        }
        if (d.isProcessed()) {
            throw new InvalidOperationException("Donation already processed");
        }
        if (status == DonationStatus.PENDING) {
            throw new InvalidOperationException("Choose accepted or rejected");
        }
        d.setStatus(status);
        d.setProcessed(true);
        if (status == DonationStatus.ACCEPTED) {
            if (uc + d.getUnits() > units.length) {
                throw new InvalidOperationException("Blood-unit capacity reached");
            }
            for (int i = 0; i < d.getUnits(); i++) {
                units[uc++] = new BloodUnit(unitIds.next(), d.getId(), d.getBloodGroup(), d.getDate(), d.getDate().plusDays(35), BloodUnitStatus.AVAILABLE);
            }
            saveUnits();
        }
        saveDonations();
    }

    public synchronized Donation findDonation(String id) {
        for (int i = 0; i < doc; i++) {
            if (donations[i].getId().equalsIgnoreCase(id)) {
                return donations[i];
            }
        }
        return null;
    }

    public synchronized BloodRequest requestBlood(String hospitalId, BloodGroup group, int quantity, RequestPriority priority) {
        HospitalUser h = findHospital(hospitalId);
        if (h == null || h.getStatus() != HospitalStatus.APPROVED) {
            throw new IllegalArgumentException("Hospital is not approved");
        }
        if (quantity < 1 || quantity > 100) {
            throw new IllegalArgumentException("Quantity must be 1-100");
        }
        if (rc == requests.length) {
            throw new IllegalStateException("Request capacity reached");
        }
        BloodRequest r = new BloodRequest(requestIds.next(), hospitalId, group, quantity, LocalDate.now(), priority, RequestStatus.PENDING, 0, "");
        requests[rc++] = r;
        saveRequests();
        return r;
    }

    public synchronized BloodRequest findRequest(String id) {
        for (int i = 0; i < rc; i++) {
            if (requests[i].getId().equalsIgnoreCase(id)) {
                return requests[i];
            }
        }
        return null;
    }

    public synchronized void decideRequest(String id, boolean approve, String reason) throws InvalidOperationException {
        BloodRequest r = findRequest(id);
        if (r == null) {
            throw new IllegalArgumentException("Request not found");
        }
        if (r.getStatus() != RequestStatus.PENDING) {
            throw new InvalidOperationException("Only pending requests can be reviewed");
        }
        r.setStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        r.setDecision(reason);
        saveRequests();
    }

    public synchronized int available(BloodGroup group) {
        int n = 0;
        for (int i = 0; i < uc; i++) {
            if (units[i].getGroup() == group && units[i].isAvailableToday()) {
                n++;
            }
        }
        return n;
    }

    public synchronized void issueBlood(String requestId, int quantity) throws InvalidOperationException, InsufficientStockException {
        BloodRequest r = findRequest(requestId);
        if (r == null) {
            throw new IllegalArgumentException("Request not found");
        }
        if (r.getStatus() != RequestStatus.APPROVED && r.getStatus() != RequestStatus.PARTIALLY_FULFILLED) {
            throw new InvalidOperationException("Request must be approved or partially fulfilled");
        }
        if (quantity < 1 || quantity > r.getRemainingUnits()) {
            throw new InvalidOperationException("Quantity exceeds remaining request");
        }
        if (available(r.getGroup()) < quantity) {
            throw new InsufficientStockException("Not enough available " + r.getGroup().label() + " units");
        }
        if (ic + quantity > issues.length) {
            throw new InvalidOperationException("Issue record capacity reached");
        }
        for (int k = 0; k < quantity; k++) {
            BloodUnit chosen = null;
            for (int i = 0; i < uc; i++) {
                if (units[i].getGroup() == r.getGroup() && units[i].isAvailableToday()) {
                    chosen = units[i];
                    break;
                }
            }
            if (chosen == null) {
                throw new InsufficientStockException("Stock changed; retry");
            }
            chosen.setStatus(BloodUnitStatus.ISSUED);
            issues[ic++] = new BloodIssue(issueIds.next(), r.getId(), r.getHospitalId(), chosen.getId(), LocalDate.now());
            r.addIssuedUnits(1);
        }
        saveUnits();
        saveIssues();
        saveRequests();
    }

    public synchronized void setThreshold(BloodGroup group, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }
        thresholds[group.ordinal()] = n;
    }

    public synchronized int threshold(BloodGroup group) {
        return thresholds[group.ordinal()];
    }

    public synchronized void printInventory() {
        System.out.println("\n--- INVENTORY (eligible available units) ---");
        for (BloodGroup g : BloodGroup.values()) {
            System.out.printf("%-5s : %d%s%n", g.label(), available(g), available(g) < threshold(g) ? "  **LOW STOCK**" : "");
        }
    }

    public synchronized void printHospitals() {
        if (hc == 0) {
            System.out.println("No hospitals registered.");
        }
        for (int i = 0; i < hc; i++) {
            System.out.printf("%s | %s | %s | username=%s%n", hospitals[i].getId(), hospitals[i].getName(), hospitals[i].getStatus(), hospitals[i].getUsername());
        }
    }

    public synchronized void printDonors() {
        for (int i = 0; i < dc; i++) {
            System.out.printf("%s | %s | %s | age %d | %s%n", donors[i].getId(), donors[i].getName(), donors[i].getBloodGroup().label(), donors[i].getAge(), donors[i].getPhone());
        }
        if (dc == 0) {
            System.out.println("No donors.");
        }
    }

    public synchronized void printDonations() {
        for (int i = 0; i < doc; i++) {
            System.out.printf("%s | donor=%s | %s | units=%d | %s | processed=%s%n", donations[i].getId(), donations[i].getDonorId(), donations[i].getStatus(), donations[i].getUnits(), donations[i].getDate(), donations[i].isProcessed());
        }
        if (doc == 0) {
            System.out.println("No donations.");
        }
    }

    public synchronized void printRequests() {
        for (int i = 0; i < rc; i++) {
            System.out.printf("%s | hospital=%s | %s | units=%d | issued=%d | %s | %s | %s%n", requests[i].getId(), requests[i].getHospitalId(), requests[i].getGroup().label(), requests[i].getUnits(), requests[i].getIssuedUnits(), requests[i].getPriority(), requests[i].getStatus(), requests[i].getDate());
        }
        if (rc == 0) {
            System.out.println("No requests.");
        }
    }

    public synchronized void printHospitalRequests(String hid) {
        boolean any = false;
        for (int i = 0; i < rc; i++) {
            if (requests[i].getHospitalId().equals(hid)) {
                any = true;
                System.out.printf("%s | %s | requested=%d | issued=%d | %s | %s%n", requests[i].getId(), requests[i].getGroup().label(), requests[i].getUnits(), requests[i].getIssuedUnits(), requests[i].getStatus(), requests[i].getPriority());
            }
        }
        if (!any) {
            System.out.println("No requests yet.");
        }
    }

    public synchronized void printHospitalIssues(String hid) {
        boolean any = false;
        for (int i = 0; i < ic; i++) {
            if (issues[i].getHospitalId().equals(hid)) {
                any = true;
                System.out.printf("%s | request=%s | unit=%s | %s%n", issues[i].getId(), issues[i].getRequestId(), issues[i].getUnitId(), issues[i].getDate());
            }
        }
        if (!any) {
            System.out.println("No issue records for this hospital.");
        }
    }

    public synchronized void printIssues() {
        for (int i = 0; i < ic; i++) {
            System.out.printf("%s | request=%s | hospital=%s | unit=%s | %s%n", issues[i].getId(), issues[i].getRequestId(), issues[i].getHospitalId(), issues[i].getUnitId(), issues[i].getDate());
        }
        if (ic == 0) {
            System.out.println("No issues.");
        }
    }

    public synchronized void printReport() {
        int pendingH = 0;
        int pendingR = 0;
        int accepted = 0;
        for (int i = 0; i < hc; i++) {
            if (hospitals[i].getStatus() == HospitalStatus.PENDING) {
                pendingH++;
            }
        }
        for (int i = 0; i < rc; i++) {
            if (requests[i].getStatus() == RequestStatus.PENDING) {
                pendingR++;
            }
        }
        for (int i = 0; i < doc; i++) {
            if (donations[i].getStatus() == DonationStatus.ACCEPTED) {
                accepted++;
            }
        }
        System.out.println("\n--- SUMMARY REPORT ---\nHospitals: " + hc + " (pending " + pendingH + ")\nDonors: " + dc + "\nDonations: " + doc + " (accepted " + accepted + ")\nBlood units created: " + uc + "\nRequests: " + rc + " (pending " + pendingR + ")\nIssues: " + ic);
        printInventory();
    }

    public synchronized BloodUnit[] unitSnapshot() {
        return Arrays.copyOf(units, uc);
    }

    public synchronized int hospitalCount() {
        return hc;
    }

    public synchronized int donorCount() {
        return dc;
    }

    public synchronized int donationCount() {
        return doc;
    }

    public synchronized int requestCount() {
        return rc;
    }

    private void saveHospitals() {
        List<String> a = new ArrayList<>();
        for (int i = 0; i < hc; i++) {
            HospitalUser h = hospitals[i];
            a.add(String.join("|",
                FileManager.enc(h.getId()),
                FileManager.enc(h.getName()),
                FileManager.enc(h.getContactPerson()),
                FileManager.enc(h.getPhone()),
                FileManager.enc(h.getAddress()),
                FileManager.enc(h.getUsername()),
                FileManager.enc(h.getPassword()),
                h.getStatus().name()
            ));
        }
        files.write("hospitals.txt", a);
    }

    private void saveDonors() {
        List<String> a = new ArrayList<>();
        for (int i = 0; i < dc; i++) {
            Donor d = donors[i];
            a.add(String.join("|",
                d.getId(),
                FileManager.enc(d.getName()),
                "" + d.getAge(),
                FileManager.enc(d.getPhone()),
                d.getBloodGroup().name()
            ));
        }
        files.write("donors.txt", a);
    }

    private void saveDonations() {
        List<String> a = new ArrayList<>();
        for (int i = 0; i < doc; i++) {
            Donation d = donations[i];
            a.add(String.join("|",
                d.getId(),
                d.getDonorId(),
                d.getBloodGroup().name(),
                "" + d.getUnits(),
                d.getDate().toString(),
                d.getStatus().name(),
                "" + d.isProcessed()
            ));
        }
        files.write("donations.txt", a);
    }

    private void saveUnits() {
        List<String> a = new ArrayList<>();
        for (int i = 0; i < uc; i++) {
            BloodUnit u = units[i];
            a.add(String.join("|",
                u.getId(),
                u.getDonationId(),
                u.getGroup().name(),
                u.getCollected().toString(),
                u.getExpiry().toString(),
                u.getStatus().name()
            ));
        }
        files.write("units.txt", a);
    }

    private void saveRequests() {
        List<String> a = new ArrayList<>();
        for (int i = 0; i < rc; i++) {
            BloodRequest r = requests[i];
            a.add(String.join("|",
                r.getId(),
                r.getHospitalId(),
                r.getGroup().name(),
                "" + r.getUnits(),
                r.getDate().toString(),
                r.getPriority().name(),
                r.getStatus().name(),
                "" + r.getIssuedUnits(),
                FileManager.enc(r.getDecision())
            ));
        }
        files.write("requests.txt", a);
    }

    private void saveIssues() {
        List<String> a = new ArrayList<>();
        for (int i = 0; i < ic; i++) {
            BloodIssue x = issues[i];
            a.add(String.join("|",
                x.getId(),
                x.getRequestId(),
                x.getHospitalId(),
                x.getUnitId(),
                x.getDate().toString()
            ));
        }
        files.write("issues.txt", a);
    }

    private void load() {
        loadHospitals();
        loadDonors();
        loadDonations();
        loadUnits();
        loadRequests();
        loadIssues();
    }

    private void loadHospitals() {
        for (String l : files.read("hospitals.txt")) {
            try {
                String[] p = FileManager.split(l);
                HospitalUser h = new HospitalUser(
                    FileManager.dec(p[0]),
                    FileManager.dec(p[1]),
                    FileManager.dec(p[2]),
                    FileManager.dec(p[3]),
                    FileManager.dec(p[4]),
                    FileManager.dec(p[5]),
                    FileManager.dec(p[6]),
                    HospitalStatus.valueOf(p[7])
                );
                if (hc < hospitals.length) {
                    hospitals[hc++] = h;
                }
                advance(hospitalIds, h.getId(), "H");
            } catch (Exception e) {
                System.err.println("Skipping invalid hospital record.");
            }
        }
    }

    private void loadDonors() {
        for (String l : files.read("donors.txt")) {
            try {
                String[] p = FileManager.split(l);
                Donor d = new Donor(
                    p[0],
                    FileManager.dec(p[1]),
                    Integer.parseInt(p[2]),
                    FileManager.dec(p[3]),
                    BloodGroup.valueOf(p[4])
                );
                if (dc < donors.length) {
                    donors[dc++] = d;
                }
                advance(donorIds, d.getId(), "D");
            } catch (Exception e) {
                System.err.println("Skipping invalid donor record.");
            }
        }
    }

    private void loadDonations() {
        for (String l : files.read("donations.txt")) {
            try {
                String[] p = FileManager.split(l);
                Donation d = new Donation(
                    p[0],
                    p[1],
                    BloodGroup.valueOf(p[2]),
                    Integer.parseInt(p[3]),
                    LocalDate.parse(p[4]),
                    DonationStatus.valueOf(p[5]),
                    Boolean.parseBoolean(p[6])
                );
                if (doc < donations.length) {
                    donations[doc++] = d;
                }
                advance(donationIds, d.getId(), "DN");
            } catch (Exception e) {
                System.err.println("Skipping invalid donation record.");
            }
        }
    }

    private void loadUnits() {
        for (String l : files.read("units.txt")) {
            try {
                String[] p = FileManager.split(l);
                BloodUnit u = new BloodUnit(
                    p[0],
                    p[1],
                    BloodGroup.valueOf(p[2]),
                    LocalDate.parse(p[3]),
                    LocalDate.parse(p[4]),
                    BloodUnitStatus.valueOf(p[5])
                );
                if (uc < units.length) {
                    units[uc++] = u;
                }
                advance(unitIds, u.getId(), "U");
            } catch (Exception e) {
                System.err.println("Skipping invalid blood-unit record.");
            }
        }
    }

    private void loadRequests() {
        for (String l : files.read("requests.txt")) {
            try {
                String[] p = FileManager.split(l);
                BloodRequest r = new BloodRequest(
                    p[0],
                    p[1],
                    BloodGroup.valueOf(p[2]),
                    Integer.parseInt(p[3]),
                    LocalDate.parse(p[4]),
                    RequestPriority.valueOf(p[5]),
                    RequestStatus.valueOf(p[6]),
                    Integer.parseInt(p[7]),
                    FileManager.dec(p[8])
                );
                if (rc < requests.length) {
                    requests[rc++] = r;
                }
                advance(requestIds, r.getId(), "R");
            } catch (Exception e) {
                System.err.println("Skipping invalid request record.");
            }
        }
    }

    private void loadIssues() {
        for (String l : files.read("issues.txt")) {
            try {
                String[] p = FileManager.split(l);
                BloodIssue x = new BloodIssue(
                    p[0],
                    p[1],
                    p[2],
                    p[3],
                    LocalDate.parse(p[4])
                );
                if (ic < issues.length) {
                    issues[ic++] = x;
                }
                advance(issueIds, x.getId(), "I");
            } catch (Exception e) {
                System.err.println("Skipping invalid issue record.");
            }
        }
    }

    private void advance(IdGenerator g, String id, String prefix) {
        try {
            long n = Long.parseLong(id.substring(prefix.length())) + 1;
            while (true) {
                String candidate = g.next();
                long c = Long.parseLong(candidate.substring(prefix.length()));
                if (c >= n) {
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }
}