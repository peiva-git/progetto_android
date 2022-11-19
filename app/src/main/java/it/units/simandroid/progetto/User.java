package it.units.simandroid.progetto;

import androidx.annotation.NonNull;

import java.util.Objects;

public class User {

    @NonNull
    private String email;
    @NonNull
    private String name;
    @NonNull
    private String surname;
    @NonNull
    private String id;

    public User(@NonNull String email, @NonNull String name, @NonNull String surname, @NonNull String id) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.id = id;
    }

    // needed for realtime Firebase database
    public User() {}

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getSurname() {
        return surname;
    }

    public void setSurname(@NonNull String surname) {
        this.surname = surname;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
