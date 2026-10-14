# Blood Bank Management System — Complete Source Code

## `src/bloodbank/Main.java`

```java
package bloodbank;
import bloodbank.model.*;
import bloodbank.service.BloodBankSystem;
import bloodbank.thread.LowStockMonitor;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {
 private static final Scanner sc=new Scanner(System.in);
 private static final BloodBankSystem app=new BloodBankSystem();
 private static final String ADMIN_USER="admin", ADMIN_PASS="admin123";
 public static void main(String[] args){
  LowStockMonitor monitor=new LowStockMonitor(app); Thread thread=new Thread(monitor,"LowStockMonitor"); thread.setDaemon(true); thread.start();
  System.out.println("=== BLOOD BANK MANAGEMENT SYSTEM ===");
  System.out.println("Demo admin credentials: admin / admin123 (change for any real deployment)");
  boolean run=true;
  while(run){System.out.println("\n1. Hospital\n2. Admin\n0. Exit");int c=readInt("Choose: ");try{switch(c){case 1:hospitalEntry();break;case 2:adminLogin();break;case 0:run=false;break;default:System.out.println("Invalid choice.");}}catch(Exception e){System.out.println("Error: "+e.getMessage());}}
  monitor.stop();thread.interrupt();sc.close();System.out.println("Goodbye.");
 }
 private static void hospitalEntry(){
  System.out.println("\n1. Register hospital\n2. Hospital login\n0. Back");int c=readInt("Choose: ");
  if(c==1){HospitalUser h=app.registerHospital(read("Hospital name: "),read("Contact person: "),read("Phone: "),read("Address: "),read("Username: "),read("Password: "));System.out.println("Registration submitted. Hospital ID: "+h.getId()+" | Status: PENDING. Ask admin to approve it.");}
  else if(c==2){String u=read("Username: "),p=read("Password: ");HospitalUser h=app.hospitalLogin(u,p);if(h==null)System.out.println("Invalid username or password.");else hospitalMenu(h);}else if(c!=0)System.out.println("Invalid choice.");
 }
 private static void hospitalMenu(HospitalUser h){
  boolean run=true;while(run){System.out.println("\n--- HOSPITAL DASHBOARD: "+h.getName()+" ---\n1. View profile\n2. View blood stock\n3. Request blood\n4. View my requests\n5. View issue records\n0. Logout");int c=readInt("Choose: ");try{switch(c){
   case 1:System.out.println(h.displayName()+" | ID="+h.getId()+" | status="+h.getStatus()+" | contact="+h.getContactPerson()+" | phone="+h.getPhone()+" | address="+h.getAddress());break;
   case 2:app.printInventory();break;
   case 3:BloodGroup g=chooseGroup();int n=readInt("Units required: ");int pv=readInt("Priority (1 Normal, 2 Emergency): ");RequestPriority pr=pv==2?RequestPriority.EMERGENCY:RequestPriority.NORMAL;BloodRequest r=app.requestBlood(h.getId(),g,n,pr);System.out.println("Request submitted: "+r.getId()+" | PENDING");break;
   case 4:app.printHospitalRequests(h.getId());break;case 5:app.printHospitalIssues(h.getId());break;case 0:run=false;break;default:System.out.println("Invalid choice.");
  }}catch(Exception e){System.out.println("Error: "+e.getMessage());}}
 }
 private static void adminLogin(){String u=read("Admin username: "),p=read("Admin password: ");if(!ADMIN_USER.equals(u)||!ADMIN_PASS.equals(p)){System.out.println("Invalid admin credentials.");return;}Admin admin=new Admin("ADMIN-1","System Administrator",u,p);System.out.println("Welcome, "+admin.displayName());adminMenu();}
 private static void adminMenu(){
  boolean run=true;while(run){System.out.println("\n--- ADMIN DASHBOARD ---\n1. View hospitals\n2. Approve/reject hospital\n3. Register donor\n4. View donors\n5. Record donation\n6. View donations\n7. Accept/reject donation\n8. View inventory\n9. Set low-stock threshold\n10. View requests\n11. Approve/reject request\n12. Issue blood\n13. View issue records\n14. Summary report\n0. Logout");int c=readInt("Choose: ");try{switch(c){
   case 1:app.printHospitals();break;
   case 2:app.printHospitals();String hid=read("Hospital ID: ");int hd=readInt("1 Approve, 2 Reject: ");if(hd!=1&&hd!=2)throw new IllegalArgumentException("Choose 1 or 2");app.decideHospital(hid,hd==1?HospitalStatus.APPROVED:HospitalStatus.REJECTED);System.out.println("Hospital status updated.");break;
   case 3:Donor d=app.addDonor(read("Donor name: "),readInt("Age: "),read("Phone: "),chooseGroup());System.out.println("Donor registered. ID: "+d.getId());break;
   case 4:app.printDonors();break;
   case 5:app.printDonors();String did=read("Donor ID: ");int qty=readInt("Donation units (1-5): ");Donation don=app.recordDonation(did,qty,LocalDate.now());System.out.println("Donation recorded: "+don.getId()+" | PENDING screening");break;
   case 6:app.printDonations();break;
   case 7:app.printDonations();String dn=read("Donation ID: ");int dec=readInt("1 Accept, 2 Reject: ");if(dec!=1&&dec!=2)throw new IllegalArgumentException("Choose 1 or 2");app.decideDonation(dn,dec==1?DonationStatus.ACCEPTED:DonationStatus.REJECTED);System.out.println("Donation decision saved.");break;
   case 8:app.printInventory();break;
   case 9:BloodGroup tg=chooseGroup();app.setThreshold(tg,readInt("New low-stock threshold: "));System.out.println("Threshold updated.");break;
   case 10:app.printRequests();break;
   case 11:app.printRequests();String rid=read("Request ID: ");int rd=readInt("1 Approve, 2 Reject: ");if(rd!=1&&rd!=2)throw new IllegalArgumentException("Choose 1 or 2");app.decideRequest(rid,rd==1,read("Decision note/reason: "));System.out.println("Request reviewed.");break;
   case 12:app.printRequests();String ir=read("Approved request ID: ");int iq=readInt("Units to issue: ");app.issueBlood(ir,iq);System.out.println("Issue recorded; inventory updated.");break;
   case 13:app.printIssues();break;case 14:app.printReport();break;case 0:run=false;break;default:System.out.println("Invalid choice.");
  }}catch(Exception e){System.out.println("Error: "+e.getMessage());}}
 }
 private static BloodGroup chooseGroup(){System.out.println("Blood groups:");BloodGroup[] gs=BloodGroup.values();for(int i=0;i<gs.length;i++)System.out.println((i+1)+". "+gs[i].label());return BloodGroup.fromNumber(readInt("Choose blood group (1-8): "));}
 private static String read(String prompt){System.out.print(prompt);return sc.nextLine().trim();}
 private static int readInt(String prompt){while(true){try{System.out.print(prompt);return Integer.parseInt(sc.nextLine().trim());}catch(NumberFormatException e){System.out.println("Enter a valid whole number.");}}}
}

```

## `src/bloodbank/exception/InsufficientStockException.java`

```java
package bloodbank.exception;
public class InsufficientStockException extends Exception { private static final long serialVersionUID = 1L; public InsufficientStockException(String message){super(message);} }
```

## `src/bloodbank/exception/InvalidOperationException.java`

```java
package bloodbank.exception;
public class InvalidOperationException extends Exception { private static final long serialVersionUID = 1L; public InvalidOperationException(String message){super(message);} }
```

## `src/bloodbank/model/Admin.java`

```java
package bloodbank.model;
public class Admin extends User {
    public Admin(String id,String name,String username,String password){super(id,name,username,password);}
    @Override public String getRole(){return "ADMIN";}
}
```

## `src/bloodbank/model/BloodGroup.java`

```java
package bloodbank.model;
public enum BloodGroup { A_POS, A_NEG, B_POS, B_NEG, AB_POS, AB_NEG, O_POS, O_NEG;
    public static BloodGroup fromNumber(int n) { if(n<1||n>values().length) throw new IllegalArgumentException("Choose 1-8"); return values()[n-1]; }
    public String label(){ return name().replace("_POS","+").replace("_NEG","-"); }
    public static BloodGroup fromLabel(String s){ for(BloodGroup g:values()) if(g.label().equalsIgnoreCase(s)||g.name().equalsIgnoreCase(s)) return g; throw new IllegalArgumentException("Invalid blood group: "+s); }
}
```

## `src/bloodbank/model/BloodIssue.java`

```java
package bloodbank.model;
import java.time.LocalDate;
public class BloodIssue {
    private final String id,requestId,hospitalId,unitId; private final LocalDate date;
    public BloodIssue(String id,String requestId,String hospitalId,String unitId,LocalDate date){this.id=id;this.requestId=requestId;this.hospitalId=hospitalId;this.unitId=unitId;this.date=date;}
    public String getId(){return id;} public String getRequestId(){return requestId;} public String getHospitalId(){return hospitalId;} public String getUnitId(){return unitId;} public LocalDate getDate(){return date;}
}
```

## `src/bloodbank/model/BloodRequest.java`

```java
package bloodbank.model;
import java.time.LocalDate;
public class BloodRequest {
    private final String id,hospitalId; private final BloodGroup group; private final int units; private final LocalDate date; private final RequestPriority priority; private RequestStatus status; private int issuedUnits; private String decision;
    public BloodRequest(String id,String hospitalId,BloodGroup group,int units,LocalDate date,RequestPriority priority,RequestStatus status,int issuedUnits,String decision){if(units<1)throw new IllegalArgumentException("Units must be positive");this.id=id;this.hospitalId=hospitalId;this.group=group;this.units=units;this.date=date;this.priority=priority;this.status=status;this.issuedUnits=issuedUnits;this.decision=decision==null?"":decision;}
    public String getId(){return id;} public String getHospitalId(){return hospitalId;} public BloodGroup getGroup(){return group;} public int getUnits(){return units;} public LocalDate getDate(){return date;} public RequestPriority getPriority(){return priority;} public RequestStatus getStatus(){return status;} public int getIssuedUnits(){return issuedUnits;} public int getRemainingUnits(){return units-issuedUnits;} public String getDecision(){return decision;}
    public void setStatus(RequestStatus s){status=s;} public void addIssuedUnits(int n){if(n<1||n>getRemainingUnits())throw new IllegalArgumentException("Invalid issue quantity");issuedUnits+=n;status=issuedUnits==units?RequestStatus.FULFILLED:RequestStatus.PARTIALLY_FULFILLED;} public void setDecision(String d){decision=d==null?"":d;}
}
```

## `src/bloodbank/model/BloodUnit.java`

```java
package bloodbank.model;
import java.time.LocalDate;
import bloodbank.persistence.FileRecord;
public class BloodUnit implements FileRecord {
    private final String id,donationId; private final BloodGroup group; private final LocalDate collected,expiry; private BloodUnitStatus status;
    public BloodUnit(String id,String donationId,BloodGroup group,LocalDate collected,LocalDate expiry,BloodUnitStatus status){this.id=id;this.donationId=donationId;this.group=group;this.collected=collected;this.expiry=expiry;this.status=status;}
    public String getId(){return id;} public String getDonationId(){return donationId;} public BloodGroup getGroup(){return group;} public LocalDate getCollected(){return collected;} public LocalDate getExpiry(){return expiry;} public BloodUnitStatus getStatus(){return status;} public void setStatus(BloodUnitStatus s){status=s;}
    @Override public String toFileLine(){return id+"|"+donationId+"|"+group.name()+"|"+collected+"|"+expiry+"|"+status.name();}
    public boolean isAvailableToday(){if(status==BloodUnitStatus.AVAILABLE&&expiry.isBefore(LocalDate.now()))status=BloodUnitStatus.EXPIRED;return status==BloodUnitStatus.AVAILABLE;}
}
```

## `src/bloodbank/model/BloodUnitStatus.java`

```java
package bloodbank.model;
public enum BloodUnitStatus { AVAILABLE, RESERVED, ISSUED, EXPIRED }
```

## `src/bloodbank/model/Donation.java`

```java
package bloodbank.model;
import java.time.LocalDate;
public class Donation {
    private final String id,donorId; private final BloodGroup bloodGroup; private final int units; private final LocalDate date; private DonationStatus status; private boolean processed;
    public Donation(String id,String donorId,BloodGroup group,int units,LocalDate date,DonationStatus status,boolean processed){if(units<1||units>5)throw new IllegalArgumentException("Units must be 1-5 in this demo");this.id=id;this.donorId=donorId;this.bloodGroup=group;this.units=units;this.date=date;this.status=status;this.processed=processed;}
    public String getId(){return id;} public String getDonorId(){return donorId;} public BloodGroup getBloodGroup(){return bloodGroup;} public int getUnits(){return units;} public LocalDate getDate(){return date;} public DonationStatus getStatus(){return status;} public boolean isProcessed(){return processed;} public void setStatus(DonationStatus s){status=s;} public void setProcessed(boolean b){processed=b;}
}
```

## `src/bloodbank/model/DonationStatus.java`

```java
package bloodbank.model;
public enum DonationStatus { PENDING, ACCEPTED, REJECTED }
```

## `src/bloodbank/model/Donor.java`

```java
package bloodbank.model;
public class Donor {
    private final String id,name,phone; private final int age; private final BloodGroup bloodGroup;
    public Donor(String id,String name,int age,String phone,BloodGroup bloodGroup){if(age<18||age>100)throw new IllegalArgumentException("Donor age must be between 18 and 100 for this demo");this.id=req(id);this.name=req(name);this.age=age;this.phone=req(phone);this.bloodGroup=java.util.Objects.requireNonNull(bloodGroup);}
    private String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("Required field is blank");return s.trim();}
    public String getId(){return id;} public String getName(){return name;} public int getAge(){return age;} public String getPhone(){return phone;} public BloodGroup getBloodGroup(){return bloodGroup;}
}
```

## `src/bloodbank/model/HospitalStatus.java`

```java
package bloodbank.model;
public enum HospitalStatus { PENDING, APPROVED, REJECTED }
```

## `src/bloodbank/model/HospitalUser.java`

```java
package bloodbank.model;
public class HospitalUser extends User {
    private final String contactPerson, phone, address; private HospitalStatus status;
    public HospitalUser(String id,String name,String contactPerson,String phone,String address,String username,String password,HospitalStatus status){super(id,name,username,password);this.contactPerson=contactPerson;this.phone=phone;this.address=address;this.status=status==null?HospitalStatus.PENDING:status;}
    @Override public String getRole(){return "HOSPITAL";}
    public String getContactPerson(){return contactPerson;} public String getPhone(){return phone;} public String getAddress(){return address;} public HospitalStatus getStatus(){return status;} public void setStatus(HospitalStatus s){if(s==null)throw new IllegalArgumentException("Status required");status=s;}
}
```

## `src/bloodbank/model/RequestPriority.java`

```java
package bloodbank.model;
public enum RequestPriority { NORMAL, EMERGENCY }
```

## `src/bloodbank/model/RequestStatus.java`

```java
package bloodbank.model;
public enum RequestStatus { PENDING, APPROVED, REJECTED, PARTIALLY_FULFILLED, FULFILLED }
```

## `src/bloodbank/model/User.java`

```java
package bloodbank.model;
public abstract class User {
    private final String id, name, username, password;
    public User(String id,String name,String username,String password){this.id=req(id,"ID");this.name=req(name,"Name");this.username=req(username,"Username");this.password=req(password,"Password");}
    public User(String id,String name,String username){this(id,name,username,"change-me");}
    private static String req(String s,String field){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException(field+" cannot be blank");return s.trim();}
    public String getId(){return id;} public String getName(){return name;} public String getUsername(){return username;} public String getPassword(){return password;}
    public abstract String getRole();
    public String displayName(){return getRole()+": "+name;}
}
```

## `src/bloodbank/persistence/FileManager.java`

```java
package bloodbank.persistence;
import java.io.*;import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.util.*;
public class FileManager {
 private final Path dir; public FileManager(){this("data");} public FileManager(String directory){dir=Paths.get(directory);try{Files.createDirectories(dir);}catch(IOException e){throw new IllegalStateException("Cannot create data folder",e);}}
 public synchronized List<String> read(String name){Path p=dir.resolve(name);if(!Files.exists(p))return new ArrayList<>();try{return Files.readAllLines(p,StandardCharsets.UTF_8);}catch(IOException e){System.err.println("Could not read "+p+": "+e.getMessage());return new ArrayList<>();}}
 public synchronized void write(String name,List<String> lines){Path p=dir.resolve(name);try(BufferedWriter w=Files.newBufferedWriter(p,StandardCharsets.UTF_8)){for(String line:lines){w.write(line);w.newLine();}}catch(IOException e){throw new IllegalStateException("Could not save "+p,e);}}
 public static String enc(String s){return Base64.getEncoder().encodeToString((s==null?"":s).getBytes(StandardCharsets.UTF_8));} public static String dec(String s){return new String(Base64.getDecoder().decode(s),StandardCharsets.UTF_8);} public static String[] split(String line){return line.split("\\|",-1);}
}
```

## `src/bloodbank/persistence/FileRecord.java`

```java
package bloodbank.persistence;
public interface FileRecord { String toFileLine(); }
```

## `src/bloodbank/service/BloodBankSystem.java`

```java
package bloodbank.service;

import bloodbank.model.*;import bloodbank.persistence.FileManager;import bloodbank.exception.*;import bloodbank.util.IdGenerator;
import java.time.LocalDate;import java.util.*;

/** Core business logic. Arrays are the primary record collections. */
public class BloodBankSystem {
 private final HospitalUser[] hospitals=new HospitalUser[50]; private final Donor[] donors=new Donor[100]; private final Donation[] donations=new Donation[200]; private final BloodUnit[] units=new BloodUnit[300]; private final BloodRequest[] requests=new BloodRequest[100]; private final BloodIssue[] issues=new BloodIssue[300];
 private int hc,dc,doc,uc,rc,ic; private final FileManager files=new FileManager();
 private final IdGenerator hospitalIds=new IdGenerator("H",1),donorIds=new IdGenerator("D",1),donationIds=new IdGenerator("DN",1),unitIds=new IdGenerator("U",1),requestIds=new IdGenerator("R",1),issueIds=new IdGenerator("I",1);
 private final int[] thresholds=new int[BloodGroup.values().length];
 public BloodBankSystem(){Arrays.fill(thresholds,3);load();}
 public synchronized HospitalUser registerHospital(String name,String contact,String phone,String address,String username,String password){if(findHospitalByUsername(username)!=null)throw new IllegalArgumentException("Username already exists");HospitalUser h=new HospitalUser(hospitalIds.next(),name,contact,phone,address,username,password,HospitalStatus.PENDING);addHospital(h);saveHospitals();return h;}
 public synchronized void addHospital(HospitalUser h){if(hc==hospitals.length)throw new IllegalStateException("Hospital capacity reached");if(findHospital(h.getId())!=null)throw new IllegalArgumentException("Duplicate hospital ID");hospitals[hc++]=h;}
 public synchronized HospitalUser findHospital(String id){for(int i=0;i<hc;i++)if(hospitals[i].getId().equalsIgnoreCase(id))return hospitals[i];return null;}
 public synchronized HospitalUser findHospitalByUsername(String u){for(int i=0;i<hc;i++)if(hospitals[i].getUsername().equalsIgnoreCase(u))return hospitals[i];return null;}
 public synchronized HospitalUser hospitalLogin(String username,String password){HospitalUser h=findHospitalByUsername(username);if(h==null||!h.getPassword().equals(password))return null;if(h.getStatus()!=HospitalStatus.APPROVED)throw new IllegalArgumentException("Hospital is not approved. Status: "+h.getStatus());return h;}
 public synchronized void decideHospital(String id,HospitalStatus status){HospitalUser h=findHospital(id);if(h==null)throw new IllegalArgumentException("Hospital not found");if(status==HospitalStatus.PENDING)throw new IllegalArgumentException("Cannot decide as pending");h.setStatus(status);saveHospitals();}
 public synchronized Donor addDonor(String name,int age,String phone,BloodGroup group){if(dc==donors.length)throw new IllegalStateException("Donor capacity reached");Donor d=new Donor(donorIds.next(),name,age,phone,group);donors[dc++]=d;saveDonors();return d;}
 public synchronized Donor findDonor(String id){for(int i=0;i<dc;i++)if(donors[i].getId().equalsIgnoreCase(id))return donors[i];return null;}
 public synchronized Donation recordDonation(String donorId,int units,LocalDate date){Donor d=findDonor(donorId);if(d==null)throw new IllegalArgumentException("Donor not found");if(doc==donations.length)throw new IllegalStateException("Donation capacity reached");Donation x=new Donation(donationIds.next(),donorId,d.getBloodGroup(),units,date,DonationStatus.PENDING,false);donations[doc++]=x;saveDonations();return x;}
 public synchronized void decideDonation(String id,DonationStatus status)throws InvalidOperationException{Donation d=findDonation(id);if(d==null)throw new IllegalArgumentException("Donation not found");if(d.isProcessed())throw new InvalidOperationException("Donation already processed");if(status==DonationStatus.PENDING)throw new InvalidOperationException("Choose accepted or rejected");d.setStatus(status);d.setProcessed(true);if(status==DonationStatus.ACCEPTED){if(uc+d.getUnits()>units.length)throw new InvalidOperationException("Blood-unit capacity reached");for(int i=0;i<d.getUnits();i++)units[uc++]=new BloodUnit(unitIds.next(),d.getId(),d.getBloodGroup(),d.getDate(),d.getDate().plusDays(35),BloodUnitStatus.AVAILABLE);saveUnits();}saveDonations();}
 public synchronized Donation findDonation(String id){for(int i=0;i<doc;i++)if(donations[i].getId().equalsIgnoreCase(id))return donations[i];return null;}
 public synchronized BloodRequest requestBlood(String hospitalId,BloodGroup group,int quantity,RequestPriority priority){HospitalUser h=findHospital(hospitalId);if(h==null||h.getStatus()!=HospitalStatus.APPROVED)throw new IllegalArgumentException("Hospital is not approved");if(quantity<1||quantity>100)throw new IllegalArgumentException("Quantity must be 1-100");if(rc==requests.length)throw new IllegalStateException("Request capacity reached");BloodRequest r=new BloodRequest(requestIds.next(),hospitalId,group,quantity,LocalDate.now(),priority,RequestStatus.PENDING,0,"");requests[rc++]=r;saveRequests();return r;}
 public synchronized BloodRequest findRequest(String id){for(int i=0;i<rc;i++)if(requests[i].getId().equalsIgnoreCase(id))return requests[i];return null;}
 public synchronized void decideRequest(String id,boolean approve,String reason)throws InvalidOperationException{BloodRequest r=findRequest(id);if(r==null)throw new IllegalArgumentException("Request not found");if(r.getStatus()!=RequestStatus.PENDING)throw new InvalidOperationException("Only pending requests can be reviewed");r.setStatus(approve?RequestStatus.APPROVED:RequestStatus.REJECTED);r.setDecision(reason);saveRequests();}
 public synchronized int available(BloodGroup group){int n=0;for(int i=0;i<uc;i++)if(units[i].getGroup()==group&&units[i].isAvailableToday())n++;return n;}
 public synchronized void issueBlood(String requestId,int quantity)throws InvalidOperationException,InsufficientStockException{BloodRequest r=findRequest(requestId);if(r==null)throw new IllegalArgumentException("Request not found");if(r.getStatus()!=RequestStatus.APPROVED&&r.getStatus()!=RequestStatus.PARTIALLY_FULFILLED)throw new InvalidOperationException("Request must be approved or partially fulfilled");if(quantity<1||quantity>r.getRemainingUnits())throw new InvalidOperationException("Quantity exceeds remaining request");if(available(r.getGroup())<quantity)throw new InsufficientStockException("Not enough available "+r.getGroup().label()+" units");if(ic+quantity>issues.length)throw new InvalidOperationException("Issue record capacity reached");for(int k=0;k<quantity;k++){BloodUnit chosen=null;for(int i=0;i<uc;i++)if(units[i].getGroup()==r.getGroup()&&units[i].isAvailableToday()){chosen=units[i];break;}if(chosen==null)throw new InsufficientStockException("Stock changed; retry");chosen.setStatus(BloodUnitStatus.ISSUED);issues[ic++]=new BloodIssue(issueIds.next(),r.getId(),r.getHospitalId(),chosen.getId(),LocalDate.now());r.addIssuedUnits(1);}saveUnits();saveIssues();saveRequests();}
 public synchronized void setThreshold(BloodGroup group,int n){if(n<0)throw new IllegalArgumentException("Threshold cannot be negative");thresholds[group.ordinal()]=n;}
 public synchronized int threshold(BloodGroup group){return thresholds[group.ordinal()];}
 public synchronized void printInventory(){System.out.println("\n--- INVENTORY (eligible available units) ---");for(BloodGroup g:BloodGroup.values())System.out.printf("%-5s : %d%s%n",g.label(),available(g),available(g)<threshold(g)?"  **LOW STOCK**":"");}
 public synchronized void printHospitals(){if(hc==0)System.out.println("No hospitals registered.");for(int i=0;i<hc;i++)System.out.printf("%s | %s | %s | username=%s%n",hospitals[i].getId(),hospitals[i].getName(),hospitals[i].getStatus(),hospitals[i].getUsername());}
 public synchronized void printDonors(){for(int i=0;i<dc;i++)System.out.printf("%s | %s | %s | age %d | %s%n",donors[i].getId(),donors[i].getName(),donors[i].getBloodGroup().label(),donors[i].getAge(),donors[i].getPhone());if(dc==0)System.out.println("No donors.");}
 public synchronized void printDonations(){for(int i=0;i<doc;i++)System.out.printf("%s | donor=%s | %s | units=%d | %s | processed=%s%n",donations[i].getId(),donations[i].getDonorId(),donations[i].getStatus(),donations[i].getUnits(),donations[i].getDate(),donations[i].isProcessed());if(doc==0)System.out.println("No donations.");}
 public synchronized void printRequests(){for(int i=0;i<rc;i++)System.out.printf("%s | hospital=%s | %s | units=%d | issued=%d | %s | %s | %s%n",requests[i].getId(),requests[i].getHospitalId(),requests[i].getGroup().label(),requests[i].getUnits(),requests[i].getIssuedUnits(),requests[i].getPriority(),requests[i].getStatus(),requests[i].getDate());if(rc==0)System.out.println("No requests.");}
 public synchronized void printHospitalRequests(String hid){boolean any=false;for(int i=0;i<rc;i++)if(requests[i].getHospitalId().equals(hid)){any=true;System.out.printf("%s | %s | requested=%d | issued=%d | %s | %s%n",requests[i].getId(),requests[i].getGroup().label(),requests[i].getUnits(),requests[i].getIssuedUnits(),requests[i].getStatus(),requests[i].getPriority());}if(!any)System.out.println("No requests yet.");}
 public synchronized void printHospitalIssues(String hid){boolean any=false;for(int i=0;i<ic;i++)if(issues[i].getHospitalId().equals(hid)){any=true;System.out.printf("%s | request=%s | unit=%s | %s%n",issues[i].getId(),issues[i].getRequestId(),issues[i].getUnitId(),issues[i].getDate());}if(!any)System.out.println("No issue records for this hospital.");}
 public synchronized void printIssues(){for(int i=0;i<ic;i++)System.out.printf("%s | request=%s | hospital=%s | unit=%s | %s%n",issues[i].getId(),issues[i].getRequestId(),issues[i].getHospitalId(),issues[i].getUnitId(),issues[i].getDate());if(ic==0)System.out.println("No issues.");}
 public synchronized void printReport(){int pendingH=0,pendingR=0,accepted=0;for(int i=0;i<hc;i++)if(hospitals[i].getStatus()==HospitalStatus.PENDING)pendingH++;for(int i=0;i<rc;i++)if(requests[i].getStatus()==RequestStatus.PENDING)pendingR++;for(int i=0;i<doc;i++)if(donations[i].getStatus()==DonationStatus.ACCEPTED)accepted++;System.out.println("\n--- SUMMARY REPORT ---\nHospitals: "+hc+" (pending "+pendingH+")\nDonors: "+dc+"\nDonations: "+doc+" (accepted "+accepted+")\nBlood units created: "+uc+"\nRequests: "+rc+" (pending "+pendingR+")\nIssues: "+ic);printInventory();}
 public synchronized BloodUnit[] unitSnapshot(){return Arrays.copyOf(units,uc);} public synchronized int hospitalCount(){return hc;} public synchronized int donorCount(){return dc;} public synchronized int donationCount(){return doc;} public synchronized int requestCount(){return rc;}
 private void saveHospitals(){List<String> a=new ArrayList<>();for(int i=0;i<hc;i++){HospitalUser h=hospitals[i];a.add(String.join("|",FileManager.enc(h.getId()),FileManager.enc(h.getName()),FileManager.enc(h.getContactPerson()),FileManager.enc(h.getPhone()),FileManager.enc(h.getAddress()),FileManager.enc(h.getUsername()),FileManager.enc(h.getPassword()),h.getStatus().name()));}files.write("hospitals.txt",a);}
 private void saveDonors(){List<String>a=new ArrayList<>();for(int i=0;i<dc;i++){Donor d=donors[i];a.add(String.join("|",d.getId(),FileManager.enc(d.getName()),""+d.getAge(),FileManager.enc(d.getPhone()),d.getBloodGroup().name()));}files.write("donors.txt",a);}
 private void saveDonations(){List<String>a=new ArrayList<>();for(int i=0;i<doc;i++){Donation d=donations[i];a.add(String.join("|",d.getId(),d.getDonorId(),d.getBloodGroup().name(),""+d.getUnits(),d.getDate().toString(),d.getStatus().name(),""+d.isProcessed()));}files.write("donations.txt",a);}
 private void saveUnits(){List<String>a=new ArrayList<>();for(int i=0;i<uc;i++){BloodUnit u=units[i];a.add(String.join("|",u.getId(),u.getDonationId(),u.getGroup().name(),u.getCollected().toString(),u.getExpiry().toString(),u.getStatus().name()));}files.write("units.txt",a);}
 private void saveRequests(){List<String>a=new ArrayList<>();for(int i=0;i<rc;i++){BloodRequest r=requests[i];a.add(String.join("|",r.getId(),r.getHospitalId(),r.getGroup().name(),""+r.getUnits(),r.getDate().toString(),r.getPriority().name(),r.getStatus().name(),""+r.getIssuedUnits(),FileManager.enc(r.getDecision())));}files.write("requests.txt",a);}
 private void saveIssues(){List<String>a=new ArrayList<>();for(int i=0;i<ic;i++){BloodIssue x=issues[i];a.add(String.join("|",x.getId(),x.getRequestId(),x.getHospitalId(),x.getUnitId(),x.getDate().toString()));}files.write("issues.txt",a);}
 private void load(){loadHospitals();loadDonors();loadDonations();loadUnits();loadRequests();loadIssues();}
 private void loadHospitals(){for(String l:files.read("hospitals.txt"))try{String[]p=FileManager.split(l);HospitalUser h=new HospitalUser(FileManager.dec(p[0]),FileManager.dec(p[1]),FileManager.dec(p[2]),FileManager.dec(p[3]),FileManager.dec(p[4]),FileManager.dec(p[5]),FileManager.dec(p[6]),HospitalStatus.valueOf(p[7]));if(hc<hospitals.length)hospitals[hc++]=h;advance(hospitalIds,h.getId(),"H");}catch(Exception e){System.err.println("Skipping invalid hospital record.");}}
 private void loadDonors(){for(String l:files.read("donors.txt"))try{String[]p=FileManager.split(l);Donor d=new Donor(p[0],FileManager.dec(p[1]),Integer.parseInt(p[2]),FileManager.dec(p[3]),BloodGroup.valueOf(p[4]));if(dc<donors.length)donors[dc++]=d;advance(donorIds,d.getId(),"D");}catch(Exception e){System.err.println("Skipping invalid donor record.");}}
 private void loadDonations(){for(String l:files.read("donations.txt"))try{String[]p=FileManager.split(l);Donation d=new Donation(p[0],p[1],BloodGroup.valueOf(p[2]),Integer.parseInt(p[3]),LocalDate.parse(p[4]),DonationStatus.valueOf(p[5]),Boolean.parseBoolean(p[6]));if(doc<donations.length)donations[doc++]=d;advance(donationIds,d.getId(),"DN");}catch(Exception e){System.err.println("Skipping invalid donation record.");}}
 private void loadUnits(){for(String l:files.read("units.txt"))try{String[]p=FileManager.split(l);BloodUnit u=new BloodUnit(p[0],p[1],BloodGroup.valueOf(p[2]),LocalDate.parse(p[3]),LocalDate.parse(p[4]),BloodUnitStatus.valueOf(p[5]));if(uc<units.length)units[uc++]=u;advance(unitIds,u.getId(),"U");}catch(Exception e){System.err.println("Skipping invalid blood-unit record.");}}
 private void loadRequests(){for(String l:files.read("requests.txt"))try{String[]p=FileManager.split(l);BloodRequest r=new BloodRequest(p[0],p[1],BloodGroup.valueOf(p[2]),Integer.parseInt(p[3]),LocalDate.parse(p[4]),RequestPriority.valueOf(p[5]),RequestStatus.valueOf(p[6]),Integer.parseInt(p[7]),FileManager.dec(p[8]));if(rc<requests.length)requests[rc++]=r;advance(requestIds,r.getId(),"R");}catch(Exception e){System.err.println("Skipping invalid request record.");}}
 private void loadIssues(){for(String l:files.read("issues.txt"))try{String[]p=FileManager.split(l);BloodIssue x=new BloodIssue(p[0],p[1],p[2],p[3],LocalDate.parse(p[4]));if(ic<issues.length)issues[ic++]=x;advance(issueIds,x.getId(),"I");}catch(Exception e){System.err.println("Skipping invalid issue record.");}}
 private void advance(IdGenerator g,String id,String prefix){try{long n=Long.parseLong(id.substring(prefix.length()))+1;while(true){String candidate=g.next();long c=Long.parseLong(candidate.substring(prefix.length()));if(c>=n)break;}}catch(Exception ignored){}}
}
```

## `src/bloodbank/thread/LowStockMonitor.java`

```java
package bloodbank.thread;
import bloodbank.model.*;import bloodbank.service.BloodBankSystem;
public class LowStockMonitor implements Runnable {
 private final BloodBankSystem system; private volatile boolean running=true;
 public LowStockMonitor(BloodBankSystem system){this.system=system;} public void stop(){running=false;}
 @Override public void run(){while(running){try{for(BloodGroup g:BloodGroup.values())if(system.available(g)<system.threshold(g))System.out.println("[LOW-STOCK MONITOR] "+g.label()+" has "+system.available(g)+" available unit(s).");Thread.sleep(60000);}catch(InterruptedException e){Thread.currentThread().interrupt();break;}catch(Exception e){System.err.println("Monitor error: "+e.getMessage());}}}
}
```

## `src/bloodbank/util/IdGenerator.java`

```java
package bloodbank.util;
import java.util.concurrent.atomic.AtomicLong;
public class IdGenerator { private final String prefix; private final AtomicLong counter; public IdGenerator(String prefix,long start){this.prefix=prefix;counter=new AtomicLong(start);} public String next(){return prefix+String.format("%04d",counter.getAndIncrement());} }
```

