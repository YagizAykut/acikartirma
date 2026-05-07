package com.acikartirma.acikartirma.bean;

import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.enums.TransactionType;
import com.acikartirma.acikartirma.entity.User;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import com.acikartirma.acikartirma.facadelocal.UserFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.TransactionFacadeLocal;

@Named
@SessionScoped
public class UserBean implements Serializable {

    @EJB
    private UserFacadeLocal userFacade;

    @EJB
    private TransactionFacadeLocal transactionFacade;

    private User currentUser;

    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    private BigDecimal loadAmount;

    public String register() {
        // 1. ADIM: BOŞLUK VE NULL KONTROLÜ
        if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                phoneNumber == null || phoneNumber.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Kayıt Başarısız: Lütfen tüm alanları eksiksiz doldurun!", null));
            return null;
        }

        // 2. ADIM: E-POSTA FORMAT KONTROLÜ (@ işareti var mı?)
        if (!email.trim().contains("@")) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Kayıt Başarısız: Lütfen geçerli bir e-posta adresi girin (örn: ornek@mail.com)!", null));
            return null;
        }

        // 3. ADIM: TELEFON FORMAT KONTROLÜ (Tam 10 rakam mı?)
        if (!phoneNumber.trim().matches("\\d{10}")) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Kayıt Başarısız: Telefon numarası 10 haneli rakamlardan oluşmalıdır (örn: 5555555555)!", null));
            return null;
        }

        // 4. ADIM: KAYIT İŞLEMİ VE BENZERSİZLİK (UNIQUE) KONTROLÜ
        try {
            User newUser = new User();
            newUser.setUsername(username.trim());
            newUser.setPassword(password.trim());
            newUser.setEmail(email.trim());
            newUser.setFirstName(firstName.trim());
            newUser.setLastName(lastName.trim());
            newUser.setPhoneNumber(phoneNumber.trim());

            userFacade.create(newUser);
            return "login?faces-redirect=true";

        } catch (Exception e) {
            // Veritabanında (Entity'de unique=true olan) aynı veriden zaten varsa
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Kayıt Başarısız: Bu kullanıcı adı, e-posta veya telefon numarası zaten sistemde kayıtlı!", null));
            return null;
        }
    }

    public String login() {
        User user = userFacade.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;

            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("valid_user", currentUser);

            return "index?faces-redirect=true";
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Giriş Başarısız: Kullanıcı adı veya şifre hatalı!", null));
        return "login?error=true";
    }

    public String logout() {
        currentUser = null;

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        return "login?faces-redirect=true";
    }

    public String loadBalance() {
        if (loadAmount != null && loadAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentBalance = currentUser.getBalance();
            currentUser.setBalance(currentBalance.add(loadAmount));

            userFacade.update(currentUser);

            Transaction tx = new Transaction();
            tx.setAmount(loadAmount);
            tx.setType(TransactionType.DEPOSIT);
            tx.setUser(currentUser);
            transactionFacade.create(tx);

            loadAmount = null;
        }
        return "index?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        if (currentUser != null) {
            currentUser = userFacade.findByUsername(currentUser.getUsername());
        }
        return currentUser;
    }

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