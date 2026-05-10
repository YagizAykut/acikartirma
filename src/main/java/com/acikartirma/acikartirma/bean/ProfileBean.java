package com.acikartirma.acikartirma.bean;

import com.acikartirma.acikartirma.entity.Bid;
import com.acikartirma.acikartirma.entity.Transaction;
import com.acikartirma.acikartirma.entity.User;
import com.acikartirma.acikartirma.facadelocal.BidFacadeLocal;
import com.acikartirma.acikartirma.facadelocal.TransactionFacadeLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
@SuppressWarnings("unused")
public class ProfileBean implements Serializable {

    @EJB
    private TransactionFacadeLocal transactionFacade;

    @EJB
    private BidFacadeLocal bidFacade;

    @Inject
    private UserBean userBean;

    private List<Transaction> transactions;
    private List<Bid> bids;

    @PostConstruct
    public void init() {
        User currentUser = userBean.getCurrentUser();
        if (currentUser != null) {

            transactions = transactionFacade.findTransactionsByUser(currentUser.getId());
            bids = bidFacade.findBidsByUser(currentUser.getId());
        }
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<Bid> getBids() {
        return bids;
    }
}