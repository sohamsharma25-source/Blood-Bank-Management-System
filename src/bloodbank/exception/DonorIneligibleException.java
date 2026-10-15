package bloodbank.exception;

import java.time.LocalDate;

public class DonorIneligibleException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String donorId;
    private final LocalDate lastDonationDate;
    private final LocalDate nextEligibleDate;
    private final long daysRemaining;

    public DonorIneligibleException(String message) {
        super(message);
        this.donorId = null;
        this.lastDonationDate = null;
        this.nextEligibleDate = null;
        this.daysRemaining = 0;
    }

    public DonorIneligibleException(String message, String donorId, LocalDate lastDonationDate, LocalDate nextEligibleDate, long daysRemaining) {
        super(message);
        this.donorId = donorId;
        this.lastDonationDate = lastDonationDate;
        this.nextEligibleDate = nextEligibleDate;
        this.daysRemaining = daysRemaining;
    }

    public String getDonorId() {
        return donorId;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public LocalDate getNextEligibleDate() {
        return nextEligibleDate;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }
}
