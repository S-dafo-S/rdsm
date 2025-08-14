package com.radiant.court.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

public class CourtTreeNode {
   @JsonProperty("fydm")
   private @NotNull Long id;
   @JsonProperty("fyjb")
   private @NotNull Long level;
   @JsonProperty("xqmc")
   private @NotNull String name;
   @JsonProperty("fyjc")
   private @NotNull String shortName;
   @JsonProperty("xq")
   private @NotNull Boolean isContainer;
   private List<CourtTreeNode> children = new ArrayList();

   public Long getId() {
      return this.id;
   }

   public Long getLevel() {
      return this.level;
   }

   public String getName() {
      return this.name;
   }

   public String getShortName() {
      return this.shortName;
   }

   public Boolean getIsContainer() {
      return this.isContainer;
   }

   public List<CourtTreeNode> getChildren() {
      return this.children;
   }

   @JsonProperty("fydm")
   public void setId(final Long id) {
      this.id = id;
   }

   @JsonProperty("fyjb")
   public void setLevel(final Long level) {
      this.level = level;
   }

   @JsonProperty("xqmc")
   public void setName(final String name) {
      this.name = name;
   }

   @JsonProperty("fyjc")
   public void setShortName(final String shortName) {
      this.shortName = shortName;
   }

   @JsonProperty("xq")
   public void setIsContainer(final Boolean isContainer) {
      this.isContainer = isContainer;
   }

   public void setChildren(final List<CourtTreeNode> children) {
      this.children = children;
   }
}
