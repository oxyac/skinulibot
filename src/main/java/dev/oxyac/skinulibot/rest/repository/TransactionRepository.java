package dev.oxyac.skinulibot.rest.repository;

import dev.oxyac.skinulibot.rest.model.Request;
import dev.oxyac.skinulibot.rest.model.Transaction;
import dev.oxyac.skinulibot.rest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    public ArrayList<Transaction> findByRequest(Request request);
}