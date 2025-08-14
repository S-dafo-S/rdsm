package com.radiant.query.domain;

import com.radiant.integrationFunction.domain.IntegrationFunction;
import com.radiant.query.domain.dto.QueryLanguage;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Table
@Entity(
   name = "java_query_implementation"
)
@PrimaryKeyJoinColumn(
   foreignKey = @ForeignKey(
   name = "java_query_implementation_pk_fk"
)
)
public class JavaQueryImplementation extends DssQueryImplementation {
   public static final String INTEGRATION_FUNCTION_FK = "java_query_implementation_int_function_fk";
   @ManyToOne
   @JoinColumn(
      name = "integration_function",
      foreignKey = @ForeignKey(
   name = "java_query_implementation_int_function_fk"
)
   )
   private IntegrationFunction integrationFunction;

   public JavaQueryImplementation(String name, DssQuery query, Boolean isActive) {
      super(name, query, isActive);
   }

   public QueryLanguage getLanguage() {
      return QueryLanguage.JAVA;
   }

   public String getImplFilename() {
      return this.integrationFunction != null ? this.integrationFunction.getOriginalFilename() : null;
   }

   public IntegrationFunction getIntegrationFunction() {
      return this.integrationFunction;
   }

   public void setIntegrationFunction(final IntegrationFunction integrationFunction) {
      this.integrationFunction = integrationFunction;
   }

   public JavaQueryImplementation() {
   }
}
