package com.fashionpin.outfitdetectionservice.repository;

import com.fashionpin.outfitdetectionservice.entity.DetectionJob;
import com.fashionpin.outfitdetectionservice.entity.DetectionJobStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetectionJobRepository extends JpaRepository<DetectionJob, String> {
    Page<DetectionJob> findByUserId(String userId, Pageable pageable);
    List<DetectionJob> findByStatus(DetectionJobStatus status);
}
