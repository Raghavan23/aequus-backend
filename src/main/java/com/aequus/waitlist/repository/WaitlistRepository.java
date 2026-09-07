package com.aequus.waitlist.repository;

import com.aequus.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, UUID> {
    Optional<Waitlist> findByEmail(String email);
    boolean existsByEmail(String email);
}
