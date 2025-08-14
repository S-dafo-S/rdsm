package com.radiant.query.util;

import java.util.List;

public class PaginationUtil {
   public static final String CURRENT_PAGE = "page";
   public static final String PAGE_SIZE = "pageSize";
   public static final String TOTAL_ELEMENTS = "total";
   public static final String DATA = "data";

   public static <T> List<T> getPage(List<T> inputList, Integer pageSize, Integer pageNum) {
      return inputList.subList(pageSize * (pageNum - 1), Math.min(pageNum * pageSize, inputList.size()));
   }
}
