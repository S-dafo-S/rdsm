package com.radiant.account;

import com.radiant.account.domain.dto.UserDto;

public class JudgePortalUserDto {
   private Long id;
   private String fullName;
   private String email;
   private String phoneNumber;
   private String address;

   public JudgePortalUserDto(UserDto user) {
      this.id = user.getId();
      if (user.getContact() != null) {
         this.fullName = user.getContact().getFullName();
         this.email = user.getContact().getEmail();
         this.phoneNumber = user.getContact().getPhoneNumber();
         this.address = user.getContact().getAddress();
      }

   }

   public Long getId() {
      return this.id;
   }

   public String getFullName() {
      return this.fullName;
   }

   public String getEmail() {
      return this.email;
   }

   public String getPhoneNumber() {
      return this.phoneNumber;
   }

   public String getAddress() {
      return this.address;
   }

   public void setId(final Long id) {
      this.id = id;
   }

   public void setFullName(final String fullName) {
      this.fullName = fullName;
   }

   public void setEmail(final String email) {
      this.email = email;
   }

   public void setPhoneNumber(final String phoneNumber) {
      this.phoneNumber = phoneNumber;
   }

   public void setAddress(final String address) {
      this.address = address;
   }

   public String toString() {
      return "JudgePortalUserDto(id=" + this.getId() + ", fullName=" + this.getFullName() + ", email=" + this.getEmail() + ", phoneNumber=" + this.getPhoneNumber() + ", address=" + this.getAddress() + ")";
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof JudgePortalUserDto)) {
         return false;
      } else {
         JudgePortalUserDto other = (JudgePortalUserDto)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$id = this.getId();
            Object other$id = other.getId();
            if (this$id == null) {
               if (other$id != null) {
                  return false;
               }
            } else if (!this$id.equals(other$id)) {
               return false;
            }

            Object this$fullName = this.getFullName();
            Object other$fullName = other.getFullName();
            if (this$fullName == null) {
               if (other$fullName != null) {
                  return false;
               }
            } else if (!this$fullName.equals(other$fullName)) {
               return false;
            }

            Object this$email = this.getEmail();
            Object other$email = other.getEmail();
            if (this$email == null) {
               if (other$email != null) {
                  return false;
               }
            } else if (!this$email.equals(other$email)) {
               return false;
            }

            Object this$phoneNumber = this.getPhoneNumber();
            Object other$phoneNumber = other.getPhoneNumber();
            if (this$phoneNumber == null) {
               if (other$phoneNumber != null) {
                  return false;
               }
            } else if (!this$phoneNumber.equals(other$phoneNumber)) {
               return false;
            }

            Object this$address = this.getAddress();
            Object other$address = other.getAddress();
            if (this$address == null) {
               if (other$address != null) {
                  return false;
               }
            } else if (!this$address.equals(other$address)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof JudgePortalUserDto;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $id = this.getId();
      result = result * 59 + ($id == null ? 43 : $id.hashCode());
      Object $fullName = this.getFullName();
      result = result * 59 + ($fullName == null ? 43 : $fullName.hashCode());
      Object $email = this.getEmail();
      result = result * 59 + ($email == null ? 43 : $email.hashCode());
      Object $phoneNumber = this.getPhoneNumber();
      result = result * 59 + ($phoneNumber == null ? 43 : $phoneNumber.hashCode());
      Object $address = this.getAddress();
      result = result * 59 + ($address == null ? 43 : $address.hashCode());
      return result;
   }

   public JudgePortalUserDto() {
   }
}
