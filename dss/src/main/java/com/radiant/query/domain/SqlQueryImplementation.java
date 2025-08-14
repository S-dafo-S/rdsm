package com.radiant.query.domain;

import com.radiant.query.domain.dto.QueryLanguage;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Table
@Entity(
   name = "sql_query_implementation"
)
@PrimaryKeyJoinColumn(
   foreignKey = @ForeignKey(
   name = "sql_query_implementation_pk_fk"
)
)
public class SqlQueryImplementation extends DssQueryImplementation {
   @Column(
      name = "code",
      columnDefinition = "TEXT"
   )
   private String code;

   public SqlQueryImplementation(String name, DssQuery query, Boolean isActive) {
      super(name, query, isActive);
   }

   public QueryLanguage getLanguage() {
      return QueryLanguage.SQL;
   }

   public String getCode() {
      return this.code;
   }

   public void setCode(final String code) {
      this.code = code;
   }

   public SqlQueryImplementation() {
   }
}
