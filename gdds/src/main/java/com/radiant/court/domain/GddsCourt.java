package com.radiant.court.domain;

import com.radiant.region.domain.RegionCourt;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
   name = "court",
   uniqueConstraints = {@UniqueConstraint(
   name = "court_name_uniq",
   columnNames = {"name"}
)}
)
public class GddsCourt extends CourtBase {
   public static final String GDDS_COURT_NAME_UNIQ = "court_name_uniq";
   @OneToOne(
      mappedBy = "court"
   )
   private RegionCourt parentRegion;
   /** @deprecated */
   @Deprecated
   @Column(
      name = "city"
   )
   private String city;
   /** @deprecated */
   @Deprecated
   @Column(
      name = "address"
   )
   private String address;
   @Column(
      name = "contact_name"
   )
   private String contactName;
   @Column(
      name = "contact_phone"
   )
   private String contactPhone;
   @Column(
      name = "contact_email"
   )
   private String contactEmail;
   @Column(
      name = "description"
   )
   private String description;
   @OneToMany(
      mappedBy = "court"
   )
   private List<GddsHostedCourt> hostedCourts = new ArrayList();
   @Column(
      name = "short_name",
      nullable = false
   )
   private String shortName;

   public GddsCourt(Long id, String name, Long level) {
      super(id, name, level);
   }

   public RegionCourt getParentRegion() {
      return this.parentRegion;
   }

   /** @deprecated */
   @Deprecated
   public String getCity() {
      return this.city;
   }

   /** @deprecated */
   @Deprecated
   public String getAddress() {
      return this.address;
   }

   public String getContactName() {
      return this.contactName;
   }

   public String getContactPhone() {
      return this.contactPhone;
   }

   public String getContactEmail() {
      return this.contactEmail;
   }

   public String getDescription() {
      return this.description;
   }

   public List<GddsHostedCourt> getHostedCourts() {
      return this.hostedCourts;
   }

   public String getShortName() {
      return this.shortName;
   }

   public void setParentRegion(final RegionCourt parentRegion) {
      this.parentRegion = parentRegion;
   }

   /** @deprecated */
   @Deprecated
   public void setCity(final String city) {
      this.city = city;
   }

   /** @deprecated */
   @Deprecated
   public void setAddress(final String address) {
      this.address = address;
   }

   public void setContactName(final String contactName) {
      this.contactName = contactName;
   }

   public void setContactPhone(final String contactPhone) {
      this.contactPhone = contactPhone;
   }

   public void setContactEmail(final String contactEmail) {
      this.contactEmail = contactEmail;
   }

   public void setDescription(final String description) {
      this.description = description;
   }

   public void setHostedCourts(final List<GddsHostedCourt> hostedCourts) {
      this.hostedCourts = hostedCourts;
   }

   public void setShortName(final String shortName) {
      this.shortName = shortName;
   }

   public GddsCourt() {
   }
}
