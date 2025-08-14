package com.radiant.program.domain.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;

public class ExternalProgramBody {
   private HashMap<String, String> rest = new HashMap();

   @JsonAnySetter
   public void putRest(String key, String value) {
      this.rest.put(key, value);
   }

   public HashMap<String, String> getRest() {
      return this.rest;
   }

   public void setRest(final HashMap<String, String> rest) {
      this.rest = rest;
   }

   public String toString() {
      return "ExternalProgramBody(rest=" + this.getRest() + ")";
   }
}
