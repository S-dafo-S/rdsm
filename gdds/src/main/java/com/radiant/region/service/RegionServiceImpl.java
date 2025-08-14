package com.radiant.region.service;

import com.radiant.exception.region.DuplicateRegionIdException;
import com.radiant.exception.region.InvalidRegionLevelException;
import com.radiant.exception.region.MissingParentRegionException;
import com.radiant.exception.region.MultipleRootRegionException;
import com.radiant.region.domain.GddsRegion;
import com.radiant.region.domain.GddsRegionRepository;
import com.radiant.region.domain.dto.OutdatedStatus;
import com.radiant.region.domain.dto.RegionDto;
import com.radiant.region.domain.dto.RegionStatusDto;
import com.radiant.region.domain.dto.RegionWithTopCourtDto;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegionServiceImpl implements RegionService {
   private static final Logger LOG = LoggerFactory.getLogger(RegionServiceImpl.class);
   @Autowired
   private GddsRegionRepository repository;

   public List<RegionWithTopCourtDto> getList() {
      return (List)this.repository.findAll().stream().map((r) -> new RegionWithTopCourtDto(r, r.getParent() != null ? r.getParent().getId() : null, (Long)r.getChildrenCourts().stream().filter((court) -> Objects.equals(court.getCourt().getLevel(), r.getLevel())).findAny().map((c) -> c.getCourt().getId()).orElse((Object)null))).collect(Collectors.toList());
   }

   public RegionStatusDto getOutdatedStatus() {
      return new RegionStatusDto(this.repository.existsByOutdatedIsTrue() ? OutdatedStatus.OUTDATED : OutdatedStatus.VALID);
   }

   public GddsRegion construct(RegionDto request) {
      if (this.repository.findById(request.getId()).isPresent()) {
         throw new DuplicateRegionIdException(request.getId());
      } else {
         boolean rootRegion = false;
         if (request.getParent() == null) {
            if (this.repository.existsByParentIsNull()) {
               throw new MultipleRootRegionException();
            }

            rootRegion = true;
         }

         GddsRegion parent = rootRegion ? null : (GddsRegion)this.repository.findById(request.getParent()).orElseThrow(() -> new MissingParentRegionException(request.getName()));
         if (!rootRegion && parent.getLevel() >= 3L) {
            throw new InvalidRegionLevelException(request.getName(), parent.getLevel() + 1L);
         } else {
            GddsRegion region = new GddsRegion();
            region.setId(request.getId());
            region.setName(request.getName());
            region.setShortName(request.getName());
            region.setLevel(rootRegion ? 1L : parent.getLevel() + 1L);
            region.setParent(parent);
            return region;
         }
      }
   }
}
