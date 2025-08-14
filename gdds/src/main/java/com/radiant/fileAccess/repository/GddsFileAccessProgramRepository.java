package com.radiant.fileAccess.repository;

import com.radiant.fileAccess.GddsFileAccessProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GddsFileAccessProgramRepository extends JpaRepository<GddsFileAccessProgram, Long> {
}
