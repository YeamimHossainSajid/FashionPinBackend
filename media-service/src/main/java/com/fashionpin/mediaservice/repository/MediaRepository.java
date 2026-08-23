package com.fashionpin.mediaservice.repository;

import com.fashionpin.mediaservice.entity.Media;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<Media, String> {
    List<Media> findByOwnerId(String ownerId);
}
