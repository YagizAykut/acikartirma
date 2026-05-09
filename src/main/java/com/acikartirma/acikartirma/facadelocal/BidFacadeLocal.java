package com.acikartirma.acikartirma.facadelocal;

import com.acikartirma.acikartirma.entity.Bid;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface BidFacadeLocal {
    void create(Bid bid);
    void update(Bid bid);
    void remove(Bid bid);
    Bid find(Object id);
    List<Bid> findAll();


    List<Bid> findBidsByProduct(Long productId);
    List<Bid> findBidsByUser(Long userId);
}