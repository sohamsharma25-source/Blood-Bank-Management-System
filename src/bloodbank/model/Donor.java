package bloodbank.model;
public class Donor {
    private final String id,name,phone; private final int age; private final BloodGroup bloodGroup;
    public Donor(String id,String name,int age,String phone,BloodGroup bloodGroup){if(age<18||age>100)throw new IllegalArgumentException("Donor age must be between 18 and 100 for this demo");this.id=req(id);this.name=req(name);this.age=age;this.phone=req(phone);this.bloodGroup=java.util.Objects.requireNonNull(bloodGroup);}
    private String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("Required field is blank");return s.trim();}
    public String getId(){return id;} public String getName(){return name;} public int getAge(){return age;} public String getPhone(){return phone;} public BloodGroup getBloodGroup(){return bloodGroup;}
}