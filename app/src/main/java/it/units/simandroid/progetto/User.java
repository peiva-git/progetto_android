package it.units.simandroid.progetto;

public class User {

    private String email;
    private String name;
    private String surname;
    private String id;

    public User(String email, String name, String surname, String id) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.id = id;
    }

    public User() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
