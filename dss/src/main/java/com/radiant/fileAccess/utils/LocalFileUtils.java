package com.radiant.fileAccess.utils;

import com.radiant.dataConnector.SizedInputStream;
import com.radiant.dataConnector.domain.LocalFileSystemDataConnector;
import com.radiant.exception.fileAccess.NoSuchFileException;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ParametersAreNonnullByDefault
public final class LocalFileUtils {
   private static final Logger LOG = LoggerFactory.getLogger(LocalFileUtils.class);

   public static @NotNull SizedInputStream getFile(LocalFileSystemDataConnector connector, FileAccessPath fileAccessPath, String path) {
      File file = new File(path);
      if (file.exists() && file.isFile()) {
         try {
            LOG.info("Starting read local file {} with length {}", path, file.length());
            return new SizedInputStream(new FileInputStream(file), file.length());
         } catch (FileNotFoundException var5) {
            throw new NoSuchFileException(path, connector.toString(), fileAccessPath.toString());
         }
      } else {
         throw new NoSuchFileException(path, connector.toString(), fileAccessPath.toString());
      }
   }

   public static List<String> listFiles(LocalFileSystemDataConnector connector, FileAccessPath fileAccessPath, String path) {
      File directory = new File(path);
      if (directory.exists() && !directory.isFile()) {
         File[] files = directory.listFiles();
         return files != null ? (List)Arrays.stream(files).filter((file) -> !file.isDirectory()).map((file) -> path + "/" + file.getName()).collect(Collectors.toList()) : Collections.emptyList();
      } else {
         throw new NoSuchFileException(path, connector.toString(), fileAccessPath.toString());
      }
   }
}
