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