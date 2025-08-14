package com.radiant.query.domain;

import com.radiant.CaseType;
import com.radiant.program.registry.ProgramEntry;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
   name = "query",
   uniqueConstraints = {@UniqueConstraint(
   name = "query_name_uniq",
   columnNames = {"name"}
)}
)
public class GddsQuery extends QueryBase {
   @ElementCollection
   @CollectionTable(
      name = "query_entry",
      joinColumns = {@JoinColumn(
   name = "query_id"
)},
      foreignKey = @ForeignKey(
   name = "query_entry_query_fk"
),
      uniqueConstraints = {@UniqueConstraint(
   name = "query_entry_classname_uniq",
   columnNames = {"class_name"}
)}
   )
   private List<ProgramEntry> programEntries = new ArrayList();
   @Column(
      name = "uploaded_doc_file_name"
   )
   private String uploadedDocFileName;
   @Column(
      name = "uploaded_sample_code_file_name"
   )
   private String uploadedSampleCodeFileName;
   public static final String NAME_UNIQ_CONSTRAINT = "query_name_uniq";
   public static final String ARG_NAME_UNIQ_CONSTRAINT = "query_argument_name_uniq";
   @ElementCollection
   @CollectionTable(
      name = "query_arguments",
      joinColumns = {@JoinColumn(
   name = "query_id"
)},
      foreignKey = @ForeignKey(
   name = "query_arguments_query_fk"
),
      uniqueConstraints = {@UniqueConstraint(
   name = "query_argument_name_uniq",
   columnNames = {"query_id", "name"}
)}
   )
   @OrderColumn(
      name = "\"order\""
   )
   private List<QueryArgument> arguments = new ArrayList();
   @OneToMany(
      mappedBy = "query"
   )
   private List<QNodeQueryImplementation> implementations = new ArrayList();
   @Enumerated(EnumType.STRING)
   @Column(
      name = "sync_status",
      nullable = false
   )
   private SyncStatus syncStatus;

   public GddsQuery(String name, QueryStatus status, CaseType caseType) {
      super(name, status, caseType);
      this.syncStatus = SyncStatus.CREATED_NOT_SYNCED;
   }

   public List<ProgramEntry> getProgramEntries() {
      return this.programEntries;
   }

   public String getUploadedDocFileName() {
      return this.uploadedDocFileName;
   }

   public String getUploadedSampleCodeFileName() {
      return this.uploadedSampleCodeFileName;
   }

   public List<QueryArgument> getArguments() {
      return this.arguments;
   }

   public List<QNodeQueryImplementation> getImplementations() {
      return this.implementations;
   }

   public SyncStatus getSyncStatus() {
      return this.syncStatus;
   }

   public void setProgramEntries(final List<ProgramEntry> programEntries) {
      this.programEntries = programEntries;
   }

   public void setUploadedDocFileName(final String uploadedDocFileName) {
      this.uploadedDocFileName = uploadedDocFileName;
   }

   public void setUploadedSampleCodeFileName(final String uploadedSampleCodeFileName) {
      this.uploadedSampleCodeFileName = uploadedSampleCodeFileName;
   }

   public void setArguments(final List<QueryArgument> arguments) {
      this.arguments = arguments;
   }

   public void setImplementations(final List<QNodeQueryImplementation> implementations) {
      this.implementations = implementations;
   }

   public void setSyncStatus(final SyncStatus syncStatus) {
      this.syncStatus = syncStatus;
   }

   public GddsQuery() {
   }
}
