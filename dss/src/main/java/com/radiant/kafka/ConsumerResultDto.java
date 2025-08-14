package com.radiant.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsumerResultDto {
   @JsonProperty("instance_id")
   String instanceId;
   @JsonProperty("base_uri")
   String baseUri;

   public String getInstanceId() {
      return this.instanceId;
   }

   public String getBaseUri() {
      return this.baseUri;
   }

   @JsonProperty("instance_id")
   public void setInstanceId(final String instanceId) {
      this.instanceId = instanceId;
   }

   @JsonProperty("base_uri")
   public void setBaseUri(final String baseUri) {
      this.baseUri = baseUri;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ConsumerResultDto)) {
         return false;
      } else {
         ConsumerResultDto other = (ConsumerResultDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$instanceId = this.getInstanceId();
            Object other$instanceId = other.getInstanceId();
            if (this$instanceId == null) {
               if (other$instanceId != null) {
                  return false;
               }
            } else if (!this$instanceId.equals(other$instanceId)) {
               return false;
            }

            Object this$baseUri = this.getBaseUri();
            Object other$baseUri = other.getBaseUri();
            if (this$baseUri == null) {
               if (other$baseUri != null) {
                  return false;
               }
            } else if (!this$baseUri.equals(other$baseUri)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof ConsumerResultDto;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $instanceId = this.getInstanceId();
      result = result * 59 + ($instanceId == null ? 43 : $instanceId.hashCode());
      Object $baseUri = this.getBaseUri();
      result = result * 59 + ($baseUri == null ? 43 : $baseUri.hashCode());
      return result;
   }
}
