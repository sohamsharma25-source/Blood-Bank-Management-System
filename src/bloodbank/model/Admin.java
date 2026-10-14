package bloodbank.model;

public class Admin extends User {
    public Admin(String id, String name, String username, String password) {
        super(id, name, username, password);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}