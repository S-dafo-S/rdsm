package com.radiant.query.domain.dto;

import com.radiant.restapi.MultipartFileResource;

public class QueryProgramDetails {
   private QueryDto metadata;
   private MultipartFileResource file;

   public QueryProgramDetails(QueryDto metadata, MultipartFileResource file) {
      this.metadata = metadata;
      this.file = file;
   }

   public QueryDto getMetadata() {
      return this.metadata;
   }

   public MultipartFileResource getFile() {
      return this.file;
   }

   public void setMetadata(final QueryDto metadata) {
      this.metadata = metadata;
   }

   public void setFile(final MultipartFileResource file) {
      this.file = file;
   }

   public QueryProgramDetails() {
   }
}
