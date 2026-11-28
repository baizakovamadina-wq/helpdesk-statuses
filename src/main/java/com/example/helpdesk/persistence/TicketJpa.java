package com.example.helpdesk.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public class TicketJpa {

    @PersistenceContext
    private EntityManager entityManager;

    public void insert(TicketEntity ticket) {
        entityManager.persist(ticket);
    }

    public Optional<TicketEntity> findById(UUID id) {
        return Optional.ofNullable(
                entityManager.find(TicketEntity.class, id)
        );
    }

    public int count(String businessKey) {
        Long result = entityManager.createQuery(
                        "SELECT COUNT(t) FROM TicketEntity t " +
                                "WHERE t.businessKey = :key",
                        Long.class
                )
                .setParameter("key", businessKey)
                .getSingleResult();

        return result.intValue();
    }

    public void flush() {
        entityManager.flush();
    }
}