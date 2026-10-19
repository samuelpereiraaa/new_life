package com.example.demo.new_life.repository;

import com.example.demo.new_life.entity.Startup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StartupRepository extends JpaRepository<Startup, Long> {
}

