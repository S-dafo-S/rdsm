package com.radiant.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsumerDto {
   String name;
   String format;
   @JsonProperty("auto.offset.reset")
   String autoOffsetReset;
   @JsonProperty("auto.commit.enable")
   String autoCommitEnable;
   @JsonProperty("fetch.min.bytes")
   String fetchMinBytes;
   @JsonProperty("consumer.request.timeout.ms")
   String consumerRequestTimeoutMs;

   public String getName() {
      return this.name;
   }

   public String getFormat() {
      return this.format;
   }

   public String getAutoOffsetReset() {
      return this.autoOffsetReset;
   }

   public String getAutoCommitEnable() {
      return this.autoCommitEnable;
   }

   public String getFetchMinBytes() {
      return this.fetchMinBytes;
   }

   public String getConsumerRequestTimeoutMs() {
      return this.consumerRequestTimeoutMs;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public void setFormat(final String format) {
      this.format = format;
   }

   @JsonProperty("auto.offset.reset")
   public void setAutoOffsetReset(final String autoOffsetReset) {
      this.autoOffsetReset = autoOffsetReset;
   }

   @JsonProperty("auto.commit.enable")
   public void setAutoCommitEnable(final String autoCommitEnable) {
      this.autoCommitEnable = autoCommitEnable;
   }

   @JsonProperty("fetch.min.bytes")
   public void setFetchMinBytes(final String fetchMinBytes) {
      this.fetchMinBytes = fetchMinBytes;
   }

   @JsonProperty("consumer.request.timeout.ms")
   public void setConsumerRequestTimeoutMs(final String consumerRequestTimeoutMs) {
      this.consumerRequestTimeoutMs = consumerRequestTimeoutMs;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ConsumerDto)) {
         return false;
      } else {
         ConsumerDto other = (ConsumerDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$name = this.getName();
            Object other$name = other.getName();
            if (this$name == null) {
               if (other$name != null) {
                  return false;
               }
            } else if (!this$name.equals(other$name)) {
               return false;
            }

            Object this$format = this.getFormat();
            Object other$format = other.getFormat();
            if (this$format == null) {
               if (other$format != null) {
                  return false;
               }
            } else if (!this$format.equals(other$format)) {
               return false;
            }

            Object this$autoOffsetReset = this.getAutoOffsetReset();
            Object other$autoOffsetReset = other.getAutoOffsetReset();
            if (this$autoOffsetReset == null) {
               if (other$autoOffsetReset != null) {
                  return false;
               }
            } else if (!this$autoOffsetReset.equals(other$autoOffsetReset)) {
               return false;
            }

            Object this$autoCommitEnable = this.getAutoCommitEnable();
            Object other$autoCommitEnable = other.getAutoCommitEnable();
            if (this$autoCommitEnable == null) {
               if (other$autoCommitEnable != null) {
                  return false;
               }
            } else if (!this$autoCommitEnable.equals(other$autoCommitEnable)) {
               return false;
            }

            Object this$fetchMinBytes = this.getFetchMinBytes();
            Object other$fetchMinBytes = other.getFetchMinBytes();
            if (this$fetchMinBytes == null) {
               if (other$fetchMinBytes != null) {
                  return false;
               }
            } else if (!this$fetchMinBytes.equals(other$fetchMinBytes)) {
               return false;
            }

            Object this$consumerRequestTimeoutMs = this.getConsumerRequestTimeoutMs();
            Object other$consumerRequestTimeoutMs = other.getConsumerRequestTimeoutMs();
            if (this$consumerRequestTimeoutMs == null) {
               if (other$consumerRequestTimeoutMs != null) {
                  return false;
               }
            } else if (!this$consumerRequestTimeoutMs.equals(other$consumerRequestTimeoutMs)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof ConsumerDto;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $name = this.getName();
      result = result * 59 + ($name == null ? 43 : $name.hashCode());
      Object $format = this.getFormat();
      result = result * 59 + ($format == null ? 43 : $format.hashCode());
      Object $autoOffsetReset = this.getAutoOffsetReset();
      result = result * 59 + ($autoOffsetReset == null ? 43 : $autoOffsetReset.hashCode());
      Object $autoCommitEnable = this.getAutoCommitEnable();
      result = result * 59 + ($autoCommitEnable == null ? 43 : $autoCommitEnable.hashCode());
      Object $fetchMinBytes = this.getFetchMinBytes();
      result = result * 59 + ($fetchMinBytes == null ? 43 : $fetchMinBytes.hashCode());
      Object $consumerRequestTimeoutMs = this.getConsumerRequestTimeoutMs();
      result = result * 59 + ($consumerRequestTimeoutMs == null ? 43 : $consumerRequestTimeoutMs.hashCode());
      return result;
   }
}
