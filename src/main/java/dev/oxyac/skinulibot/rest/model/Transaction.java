
package dev.oxyac.skinulibot.rest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "request_id", "user_id" }) })
@Setter
@Getter
public class Transaction {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Request request;
    @ManyToOne
    private User user;

    private double amount;
    private boolean isCompleted;
}
