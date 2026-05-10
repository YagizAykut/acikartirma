package com.acikartirma.acikartirma.bean;

import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.enums.TransactionType;
import com.acikartirma.acikartirma.facadelocal.TransactionFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.UserFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import org.mindrot.jbcrypt.BCrypt;

@Named
@SessionScoped
@SuppressWarnings("unused")
public class UserBean implements Serializable {

    @EJB
    private UserFacadeLocal userFacade;

    @EJB
    private TransactionFacadeLocal transactionFacade;

    private User currentUser;
    private boolean loggedIn;

    private String loginUsername;
    private String loginPassword;
    private User newUser;
    private BigDecimal loadAmount;

    public UserBean() {
        newUser = new User();
    }

    public String login() {
        User user = userFacade.findByUsername(loginUsername);
        boolean isPasswordCorrect = false;

        if (user != null && user.getPassword() != null) {
            try {
                // BCrypt, veritabanındaki şifreyi çözmeye çalışır.
                isPasswordCorrect = BCrypt.checkpw(loginPassword, user.getPassword());
            } catch (IllegalArgumentException e) {
                // Eğer veritabanındaki şifre BCrypt formatında değilse (eski düz metin şifreyse),
                // sistemin çökmesini engeller ve şifreyi geçersiz (false) sayarız.
                isPasswordCorrect = false;
            }
        }

        if (isPasswordCorrect) {
            currentUser = user;
            loggedIn = true;

            // SecurityFilter'ın (Güvenlik Filtresinin) bizi tanıması için bileti Session'a ekliyoruz
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("valid_user", user);

            return "index?faces-redirect=true";
        }

        // Şifre yanlışsa VEYA eski düz metin formatındaysa, çökme olmadan bu uyarıyı verir.
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata", "Kullanıcı adı veya şifre hatalı."));
        return null;
    }

    public String logout() {
        currentUser = null;
        loggedIn = false;

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "login?faces-redirect=true";
    }

    public String register() {
        try {
            // Şifreyi veritabanına kaydetmeden önce BCrypt ile şifreliyoruz
            String hashedPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
            newUser.setPassword(hashedPassword);

            userFacade.create(newUser);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Başarılı", "Kayıt tamamlandı. Lütfen giriş yapınız."));
            newUser = new User();
            return "login?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata", "Kayıt başarısız. Kullanıcı adı veya email kullanılıyor olabilir."));
            return null;
        }
    }

    public void loadBalance() {
        if (loadAmount != null && loadAmount.compareTo(BigDecimal.ZERO) > 0) {
            currentUser.setBalance(currentUser.getBalance().add(loadAmount));
            userFacade.update(currentUser);

            Transaction t = new Transaction();
            t.setUser(currentUser);
            t.setAmount(loadAmount);
            t.setType(TransactionType.DEPOSIT);
            transactionFacade.create(t);

            currentUser = userFacade.find(currentUser.getId());
            loadAmount = null;

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Başarılı", "Bakiye başarıyla yüklendi."));
        }
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    public boolean isLoggedIn() { return loggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }

    public String getLoginUsername() { return loginUsername; }
    public void setLoginUsername(String loginUsername) { this.loginUsername = loginUsername; }

    public String getLoginPassword() { return loginPassword; }
    public void setLoginPassword(String loginPassword) { this.loginPassword = loginPassword; }

    public User getNewUser() { return newUser; }
    public void setNewUser(User newUser) { this.newUser = newUser; }

    public BigDecimal getLoadAmount() { return loadAmount; }
    public void setLoadAmount(BigDecimal loadAmount) { this.loadAmount = loadAmount; }
}