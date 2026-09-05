package com.aequus.account.repository;

import com.aequus.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findAllByUserIdAndArchivedFalseAndIsDeletedFalseOrderByCreatedAtAsc(UUID userId);

    List<Account> findAllByUserIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID userId);

    Optional<Account> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndArchivedFalseAndIsDeletedFalse(UUID userId);
}
