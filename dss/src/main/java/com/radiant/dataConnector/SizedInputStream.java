package com.radiant.dataConnector;

import java.io.InputStream;

public class SizedInputStream {
   private final InputStream inputStream;
   private final Long contentLength;
   private final Long offset;

   public SizedInputStream(InputStream inputStream, Long contentLength) {
      this.inputStream = inputStream;
      this.contentLength = contentLength;
      this.offset = 0L;
   }

   public SizedInputStream(InputStream inputStream, Long contentLength, Long offset) {
      this.inputStream = inputStream;
      this.contentLength = contentLength;
      this.offset = offset;
   }

   public InputStream getInputStream() {
      return this.inputStream;
   }

   public Long getContentLength() {
      return this.contentLength;
   }

   public Long getOffset() {
      return this.offset;
   }
}
