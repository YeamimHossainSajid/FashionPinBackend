package com.fashionpin.authservice.repository;

import com.fashionpin.authservice.entity.OutboxEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(String status);
}
