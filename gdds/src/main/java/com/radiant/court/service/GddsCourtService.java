package com.radiant.court.service;

import com.radiant.court.domain.GddsCourt;
import com.radiant.court.domain.dto.CourtDto;
import com.radiant.court.domain.dto.CourtTreeNode;
import com.radiant.court.domain.dto.GddsCourtDto;
import com.radiant.court.domain.dto.GddsCourtListPlain;
import com.radiant.court.domain.dto.GddsHostedCourtListPlain;
import com.radiant.court.domain.dto.VersionedCourtTree;
import com.radiant.region.domain.dto.GddsRegionCourtPair;
import java.util.List;
import javax.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;

public interface GddsCourtService {
   void createInitialStructure();

   List<GddsRegionCourtPair> getCourtList(@Nullable Long levelLimit, @Nullable Long[] regions);

   List<CourtDto> getCourtListMinimalInfo();

   GddsCourtListPlain getCourtListPlain();

   CourtTreeNode getCourtTree(boolean hostedOnly);

   CourtTreeNode getCourtTree(Long regionId);

   List<CourtDto> getCourtsByRegion(Long regionId);

   void uploadTree(MultipartFile file);

   GddsCourtDto getById(Long courtId);

   GddsCourt getCourt(Long courtId);

   GddsCourtDto create(GddsCourtDto request);

   GddsCourtDto update(Long courtId, GddsCourtDto request);

   void delete(Long courtId);

   GddsCourtDto createTopWithRegion(GddsCourtDto request);

   VersionedCourtTree getGreaterCourtTreeVersion(@Nullable String versionGreater, boolean hostedOnly);

   CourtDto getCourtByName(String name);

   List<CourtDto> getCourtByPartialName(String name);

   GddsHostedCourtListPlain getHostedCourtListPlain();
}
