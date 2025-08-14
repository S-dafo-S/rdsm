package com.radiant.kafka;

import java.util.List;

public class TopicsDto {
   List<String> topics;

   public List<String> getTopics() {
      return this.topics;
   }

   public void setTopics(final List<String> topics) {
      this.topics = topics;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof TopicsDto)) {
         return false;
      } else {
         TopicsDto other = (TopicsDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$topics = this.getTopics();
            Object other$topics = other.getTopics();
            if (this$topics == null) {
               if (other$topics != null) {
                  return false;
               }
            } else if (!this$topics.equals(other$topics)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof TopicsDto;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $topics = this.getTopics();
      result = result * 59 + ($topics == null ? 43 : $topics.hashCode());
      return result;
   }
}
