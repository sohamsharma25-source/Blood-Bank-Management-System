package bloodbank.model;
import java.time.LocalDate;
public class Donation {
    private final String id,donorId; private final BloodGroup bloodGroup; private final int units; private final LocalDate date; private DonationStatus status; private boolean processed;
    public Donation(String id,String donorId,BloodGroup group,int units,LocalDate date,DonationStatus status,boolean processed){if(units<1||units>5)throw new IllegalArgumentException("Units must be 1-5 in this demo");this.id=id;this.donorId=donorId;this.bloodGroup=group;this.units=units;this.date=date;this.status=status;this.processed=processed;}
    public String getId(){return id;} public String getDonorId(){return donorId;} public BloodGroup getBloodGroup(){return bloodGroup;} public int getUnits(){return units;} public LocalDate getDate(){return date;} public DonationStatus getStatus(){return status;} public boolean isProcessed(){return processed;} public void setStatus(DonationStatus s){status=s;} public void setProcessed(boolean b){processed=b;}
}