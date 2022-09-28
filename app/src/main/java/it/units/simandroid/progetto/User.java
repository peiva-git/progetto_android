package it.units.simandroid.progetto;

public class User {

    private String email;
    private String plaintextPassword;

    public User(String email, String plaintextPassword) {
        this.email = email;
        this.plaintextPassword = plaintextPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlaintextPassword() {
        return plaintextPassword;
    }

    public void setPlaintextPassword(String plaintextPassword) {
        this.plaintextPassword = plaintextPassword;
    }
}
