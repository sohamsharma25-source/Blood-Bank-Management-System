package bloodbank.model;
public class HospitalUser extends User {
    private final String contactPerson, phone, address; private HospitalStatus status;
    public HospitalUser(String id,String name,String contactPerson,String phone,String address,String username,String password,HospitalStatus status){super(id,name,username,password);this.contactPerson=contactPerson;this.phone=phone;this.address=address;this.status=status==null?HospitalStatus.PENDING:status;}
    @Override public String getRole(){return "HOSPITAL";}
    public String getContactPerson(){return contactPerson;} public String getPhone(){return phone;} public String getAddress(){return address;} public HospitalStatus getStatus(){return status;} public void setStatus(HospitalStatus s){if(s==null)throw new IllegalArgumentException("Status required");status=s;}
}