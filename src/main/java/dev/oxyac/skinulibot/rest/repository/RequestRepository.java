package dev.oxyac.skinulibot.rest.repository;

import dev.oxyac.skinulibot.rest.model.Request;
import dev.oxyac.skinulibot.rest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

}