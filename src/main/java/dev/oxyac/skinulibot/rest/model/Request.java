package dev.oxyac.skinulibot.rest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;

@Entity
@Setter
@Getter
public class Request {
    @Id
    @GeneratedValue
    private Long id;
    private double amount;
    @ManyToOne
    public User initiatedBy;
    private String inlineQueryId;
    private Long chatId;

    @CreationTimestamp
    private Date date;

    @OneToMany(mappedBy = "request")
    private ArrayList<Transaction> transactions;
}