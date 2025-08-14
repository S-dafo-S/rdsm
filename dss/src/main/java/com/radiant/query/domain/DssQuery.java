package com.radiant.query.domain;

import com.radiant.CaseType;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
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
public class DssQuery extends QueryBase {
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
   private List<DssQueryImplementation> implementations = new ArrayList();

   public DssQuery(String name, CaseType caseType) {
      super(name, caseType);
   }

   public List<QueryArgument> getArguments() {
      return this.arguments;
   }

   public List<DssQueryImplementation> getImplementations() {
      return this.implementations;
   }

   public void setArguments(final List<QueryArgument> arguments) {
      this.arguments = arguments;
   }

   public void setImplementations(final List<DssQueryImplementation> implementations) {
      this.implementations = implementations;
   }

   public DssQuery() {
   }
}
