package com.radiant.exception.program;

import com.radiant.exception.SystemException;
import com.radiant.program.dto.IntegrationType;
import java.util.Collection;
import java.util.stream.Collectors;

public class InvalidIntegrationType extends SystemException {
   final Collection<IntegrationType> currentTypes;
   final Collection<IntegrationType> typesToSet;

   public InvalidIntegrationType(Collection<IntegrationType> currentTypes, Collection<IntegrationType> typesToSet) {
      this.currentTypes = currentTypes;
      this.typesToSet = typesToSet;
   }

   public String getErrorCode() {
      return "INVALID_INTEGRATION_TYPE";
   }

   public String getErrorMessage() {
      return "Integration type reducing is not allowed, current value: " + (this.currentTypes.isEmpty() ? "NONE" : (String)this.currentTypes.stream().map(Enum::name).collect(Collectors.joining(", ")));
   }

   public String toString() {
      return "InvalidIntegrationType(currentTypes=" + this.currentTypes + ", typesToSet=" + this.typesToSet + ")";
   }
}
