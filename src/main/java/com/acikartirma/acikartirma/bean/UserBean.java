package com.acikartirma.acikartirma.bean;

import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.entity.TransactionType;
import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.facade.TransactionFacade;
import com.acikartirma.acikartirma.facade.UserFacade;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;

@Named
@SessionScoped
public class UserBean implements Serializable {

    @EJB
    private UserFacade userFacade;

    @EJB
    private TransactionFacade transactionFacade; // FİNANSAL LOGLAMA İÇİN EKLENDİ

    private User currentUser;

    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    private BigDecimal loadAmount;

    public String register() {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPhoneNumber(phoneNumber);

        userFacade.create(newUser);
        return "login?faces-redirect=true";
    }

    public String login() {
        User user = userFacade.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return "index?faces-redirect=true";
        }
        return "login?error=true";
    }

    public String logout() {
        currentUser = null;
        return "login?faces-redirect=true";
    }

    public String loadBalance() {
        if (loadAmount != null && loadAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentBalance = currentUser.getBalance();
            currentUser.setBalance(currentBalance.add(loadAmount));

            userFacade.update(currentUser);

            // --- İŞLEMİ LOGLA (DEPOSIT) ---
            Transaction tx = new Transaction();
            tx.setAmount(loadAmount);
            tx.setType(TransactionType.DEPOSIT);
            tx.setUser(currentUser);
            transactionFacade.create(tx);
            // ------------------------------

            loadAmount = null;
        }
        return "index?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // --- GETTER VE SETTER ---
    public User getCurrentUser() { return currentUser; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public BigDecimal getLoadAmount() { return loadAmount; }
    public void setLoadAmount(BigDecimal loadAmount) { this.loadAmount = loadAmount; }
}