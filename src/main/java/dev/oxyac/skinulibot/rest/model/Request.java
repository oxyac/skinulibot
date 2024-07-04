package dev.oxyac.skinulibot.rest.model;

import jakarta.persistence.*;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Setter
public class Request {
    @Id
    @GeneratedValue
    private Long id;
    private double amount;
    @ManyToOne
    public User initiatedBy;

    @CreationTimestamp
    public Date date;

}