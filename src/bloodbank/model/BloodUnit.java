package bloodbank.model;

import java.time.LocalDate;
import bloodbank.persistence.FileRecord;

public class BloodUnit implements FileRecord {
    private final String id;
    private final String donationId;
    private final BloodGroup group;
    private final LocalDate collected;
    private final LocalDate expiry;
    private BloodUnitStatus status;

    public BloodUnit(String id, String donationId, BloodGroup group, LocalDate collected, LocalDate expiry, BloodUnitStatus status) {
        this.id = id;
        this.donationId = donationId;
        this.group = group;
        this.collected = collected;
        this.expiry = expiry;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getDonationId() {
        return donationId;
    }

    public BloodGroup getGroup() {
        return group;
    }

    public LocalDate getCollected() {
        return collected;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public BloodUnitStatus getStatus() {
        return status;
    }

    public void setStatus(BloodUnitStatus s) {
        status = s;
    }

    @Override
    public String toFileLine() {
        return id + "|" + donationId + "|" + group.name() + "|" + collected + "|" + expiry + "|" + status.name();
    }

    public boolean isAvailableToday() {
        if (status == BloodUnitStatus.AVAILABLE && expiry.isBefore(LocalDate.now())) {
            status = BloodUnitStatus.EXPIRED;
        }
        return status == BloodUnitStatus.AVAILABLE;
    }
}