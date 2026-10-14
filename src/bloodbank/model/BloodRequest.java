package bloodbank.model;

import java.time.LocalDate;

public class BloodRequest {
    private final String id;
    private final String hospitalId;
    private final BloodGroup group;
    private final int units;
    private final LocalDate date;
    private final RequestPriority priority;
    private RequestStatus status;
    private int issuedUnits;
    private String decision;

    public BloodRequest(String id, String hospitalId, BloodGroup group, int units, LocalDate date, RequestPriority priority, RequestStatus status, int issuedUnits, String decision) {
        if (units < 1) {
            throw new IllegalArgumentException("Units must be positive");
        }
        this.id = id;
        this.hospitalId = hospitalId;
        this.group = group;
        this.units = units;
        this.date = date;
        this.priority = priority;
        this.status = status;
        this.issuedUnits = issuedUnits;
        this.decision = decision == null ? "" : decision;
    }

    public String getId() {
        return id;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public BloodGroup getGroup() {
        return group;
    }

    public int getUnits() {
        return units;
    }

    public LocalDate getDate() {
        return date;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public int getIssuedUnits() {
        return issuedUnits;
    }

    public int getRemainingUnits() {
        return units - issuedUnits;
    }

    public String getDecision() {
        return decision;
    }

    public void setStatus(RequestStatus s) {
        status = s;
    }

    public void addIssuedUnits(int n) {
        if (n < 1 || n > getRemainingUnits()) {
            throw new IllegalArgumentException("Invalid issue quantity");
        }
        issuedUnits += n;
        status = issuedUnits == units ? RequestStatus.FULFILLED : RequestStatus.PARTIALLY_FULFILLED;
    }

    public void setDecision(String d) {
        decision = d == null ? "" : d;
    }
}