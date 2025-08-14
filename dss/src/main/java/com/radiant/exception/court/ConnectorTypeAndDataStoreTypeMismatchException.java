package com.radiant.exception.court;

import com.radiant.exception.SystemException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ConnectorTypeAndDataStoreTypeMismatchException extends SystemException {
   public String getErrorCode() {
      return "CONNECTOR_TYPE_AND_STORE_TYPE_MISMATCH";
   }
}
