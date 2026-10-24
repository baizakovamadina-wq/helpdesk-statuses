
package com.example.helpdesk.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class TicketJpa {

    @PersistenceContext
    private EntityManager entityManager;

    // Insert a new ticket into the database
    public void insert(TicketEntity ticket) {
        entityManager.persist(ticket);
    }

    // Count tickets with a specific business key
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

    // Send pending SQL operations to PostgreSQL
    public void flush() {
        entityManager.flush();
    }
}
