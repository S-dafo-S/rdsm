package com.radiant.exception.query;

import com.radiant.account.exception.NotFoundException;

public class NoSuchDssQueryImplementationException extends NotFoundException {
   private final Long parentQueryId;

   public NoSuchDssQueryImplementationException(Long id, Long parentQueryId) {
      super(id);
      this.parentQueryId = parentQueryId;
   }

   public String getErrorCode() {
      return "NO_SUCH_QUERY_IMPLEMENTATION";
   }

   public Long getParentQueryId() {
      return this.parentQueryId;
   }

   public String toString() {
      return "NoSuchDssQueryImplementationException(super=" + super.toString() + ", parentQueryId=" + this.getParentQueryId() + ")";
   }
}
