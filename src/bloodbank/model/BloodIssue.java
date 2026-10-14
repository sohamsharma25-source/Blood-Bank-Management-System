package bloodbank.model;

import java.time.LocalDate;

public class BloodIssue {
    private final String id;
    private final String requestId;
    private final String hospitalId;
    private final String unitId;
    private final LocalDate date;

    public BloodIssue(String id, String requestId, String hospitalId, String unitId, LocalDate date) {
        this.id = id;
        this.requestId = requestId;
        this.hospitalId = hospitalId;
        this.unitId = unitId;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public String getUnitId() {
        return unitId;
    }

    public LocalDate getDate() {
        return date;
    }
}