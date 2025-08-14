package com.radiant.query.service;

import com.radiant.exception.query.MissingQueryArgument;
import com.radiant.exception.query.UnknownQueryArgument;
import com.radiant.query.domain.DssQuery;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.TextStringBuilder;
import org.springframework.util.LinkedCaseInsensitiveMap;

public class SqlQueryArgumentResolver {
   private final DssQuery query;
   private final Map<String, String> argumentValues;
   private final Map<String, String> globalVariables;
   private final Set<String> additionalParamsKeys;
   private static final String CASE_CAT_GLOBAL_VAR = "caseCat";

   public SqlQueryArgumentResolver(DssQuery query, Map<String, String> argumentValues, Set<String> paramKeys) {
      this.query = query;
      this.argumentValues = new LinkedCaseInsensitiveMap<>();
      this.argumentValues.putAll(argumentValues);
      Map<String, String> variables = new LinkedCaseInsensitiveMap<>();
      variables.put("caseCat", query.getCaseType().toString().toLowerCase());
      this.globalVariables = variables;
      this.additionalParamsKeys = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      this.additionalParamsKeys.addAll(paramKeys);
   }

   public String execute(String input) {
      StringSubstitutor resolver = new StringSubstitutor() {
         protected String resolveVariable(String variableName, TextStringBuilder buf, int startPos, int endPos) {
            if (!SqlQueryArgumentResolver.this.globalVariables.containsKey(variableName) && SqlQueryArgumentResolver.this.query.getArguments().stream().noneMatch((arg) -> variableName.equalsIgnoreCase(arg.getName())) && !SqlQueryArgumentResolver.this.additionalParamsKeys.contains(variableName)) {
               throw new UnknownQueryArgument(variableName);
            } else if (SqlQueryArgumentResolver.this.globalVariables.containsKey(variableName)) {
               return String.format("'%s'", SqlQueryArgumentResolver.this.globalVariables.get(variableName));
            } else if (!SqlQueryArgumentResolver.this.argumentValues.containsKey(variableName)) {
               throw new MissingQueryArgument(variableName);
            } else {
               String val = (String)SqlQueryArgumentResolver.this.argumentValues.get(variableName);
               return String.format("%s", val);
            }
         }
      };
      return resolver.replace(input);
   }

   public DssQuery getQuery() {
      return this.query;
   }

   public Map<String, String> getArgumentValues() {
      return this.argumentValues;
   }

   public Map<String, String> getGlobalVariables() {
      return this.globalVariables;
   }

   public Set<String> getAdditionalParamsKeys() {
      return this.additionalParamsKeys;
   }
}
