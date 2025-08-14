package com.radiant.log.access.domain;

import java.lang.invoke.SerializedLambda;
import java.util.Date;
import org.springframework.data.jpa.domain.Specification;

public class AccessLogSpecs {
   public static Specification<AccessLog> isDateAfter(Date date) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), date);
   }

   public static Specification<AccessLog> isDateBefore(Date date) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("startTime"), date);
   }

   private static Specification<AccessLog> eqSpec(String field, String value) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
   }

   public static Specification<AccessLog> appIdEq(String apId) {
      return eqSpec("appId", apId);
   }

   public static Specification<AccessLog> sysIdEq(String sysId) {
      return eqSpec("sysId", sysId);
   }

   public static Specification<AccessLog> courtIdEq(String courtId) {
      return eqSpec("courtId", courtId);
   }

   public static Specification<AccessLog> ipEq(String ip) {
      return eqSpec("clientAddress", ip);
   }

   private static Specification<AccessLog> containsSpec(String field, String value) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(field), String.format("%%%s%%", value));
   }

   public static Specification<AccessLog> courtNameContains(String courtName) {
      return containsSpec("courtName", courtName);
   }

   public static Specification<AccessLog> usernameContains(String username) {
      return containsSpec("userName", username);
   }

   public static Specification<AccessLog> userIdContains(String userId) {
      return containsSpec("userId", userId);
   }

   public static Specification<AccessLog> pathContains(String path) {
      return containsSpec("apiPath", path);
   }

   public static Specification<AccessLog> responseIs(Integer response) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("responseCode"), response);
   }

   public static Specification<AccessLog> durationMore(int duration) {
      return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("duration"), duration);
   }

   // $FF: synthetic method
   private static Object $deserializeLambda$(SerializedLambda lambda) {
      switch (lambda.getImplMethodName()) {
         case "lambda$responseIs$77b754e4$1":
            if (lambda.getImplMethodKind() == 6 && lambda.getFunctionalInterfaceClass().equals("org/springframework/data/jpa/domain/Specification") && lambda.getFunctionalInterfaceMethodName().equals("toPredicate") && lambda.getFunctionalInterfaceMethodSignature().equals("(Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;") && lambda.getImplClass().equals("com/radiant/log/access/domain/AccessLogSpecs") && lambda.getImplMethodSignature().equals("(Ljava/lang/Integer;Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;")) {
               return (Specification)(root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("responseCode"), response);
            }
            break;
         case "lambda$eqSpec$6074914$1":
            if (lambda.getImplMethodKind() == 6 && lambda.getFunctionalInterfaceClass().equals("org/springframework/data/jpa/domain/Specification") && lambda.getFunctionalInterfaceMethodName().equals("toPredicate") && lambda.getFunctionalInterfaceMethodSignature().equals("(Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;") && lambda.getImplClass().equals("com/radiant/log/access/domain/AccessLogSpecs") && lambda.getImplMethodSignature().equals("(Ljava/lang/String;Ljava/lang/String;Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;")) {
               return (Specification)(root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
            }
            break;
         case "lambda$containsSpec$6074914$1":
            if (lambda.getImplMethodKind() == 6 && lambda.getFunctionalInterfaceClass().equals("org/springframework/data/jpa/domain/Specification") && lambda.getFunctionalInterfaceMethodName().equals("toPredicate") && lambda.getFunctionalInterfaceMethodSignature().equals("(Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;") && lambda.getImplClass().equals("com/radiant/log/access/domain/AccessLogSpecs") && lambda.getImplMethodSignature().equals("(Ljava/lang/String;Ljava/lang/String;Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;")) {
               return (Specification)(root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(field), String.format("%%%s%%", value));
            }
            break;
         case "lambda$isDateAfter$4354bd37$1":
            if (lambda.getImplMethodKind() == 6 && lambda.getFunctionalInterfaceClass().equals("org/springframework/data/jpa/domain/Specification") && lambda.getFunctionalInterfaceMethodName().equals("toPredicate") && lambda.getFunctionalInterfaceMethodSignature().equals("(Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;") && lambda.getImplClass().equals("com/radiant/log/access/domain/AccessLogSpecs") && lambda.getImplMethodSignature().equals("(Ljava/util/Date;Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;")) {
               return (Specification)(root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), date);
            }
            break;
         case "lambda$isDateBefore$4354bd37$1":
            if (lambda.getImplMethodKind() == 6 && lambda.getFunctionalInterfaceClass().equals("org/springframework/data/jpa/domain/Specification") && lambda.getFunctionalInterfaceMethodName().equals("toPredicate") && lambda.getFunctionalInterfaceMethodSignature().equals("(Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;") && lambda.getImplClass().equals("com/radiant/log/access/domain/AccessLogSpecs") && lambda.getImplMethodSignature().equals("(Ljava/util/Date;Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;")) {
               return (Specification)(root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("startTime"), date);
            }
            break;
         case "lambda$durationMore$36739b71$1":
            if (lambda.getImplMethodKind() == 6 && lambda.getFunctionalInterfaceClass().equals("org/springframework/data/jpa/domain/Specification") && lambda.getFunctionalInterfaceMethodName().equals("toPredicate") && lambda.getFunctionalInterfaceMethodSignature().equals("(Ljavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;") && lambda.getImplClass().equals("com/radiant/log/access/domain/AccessLogSpecs") && lambda.getImplMethodSignature().equals("(ILjavax/persistence/criteria/Root;Ljavax/persistence/criteria/CriteriaQuery;Ljavax/persistence/criteria/CriteriaBuilder;)Ljavax/persistence/criteria/Predicate;")) {
               return (Specification)(root, query, cb) -> cb.greaterThanOrEqualTo(root.get("duration"), duration);
            }
      }

      throw new IllegalArgumentException("Invalid lambda deserialization");
   }
}
