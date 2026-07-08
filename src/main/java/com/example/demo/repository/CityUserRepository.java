package com.example.demo.repository;

import com.example.demo.entity.CityUser;
import com.example.demo.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityUserRepository extends JpaRepository<CityUser, Long> {

    Optional<CityUser> findByUsername(String username);

    List<CityUser> findByRole(Role role);

    boolean existsByUsername(String username);

    boolean existsByBadgeNumber(String badgeNumber);
}
