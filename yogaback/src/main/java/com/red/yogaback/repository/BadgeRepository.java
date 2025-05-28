package com.red.yogaback.repository;

import com.red.yogaback.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {


}
