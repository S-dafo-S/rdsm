package com.radiant.fileAccess.service;

import com.radiant.dataConnector.SizedInputStream;
import com.radiant.dataConnector.domain.DataConnector;
import com.radiant.dataConnector.domain.FileDataConnector;
import com.radiant.dataConnector.domain.HttpFileDataConnector;
import com.radiant.dataConnector.service.DataConnectorService;
import com.radiant.exception.RdsmIOException;
import com.radiant.exception.WrongUrlException;
import com.radiant.exception.fileAccess.NoFileNameInPath;
import com.radiant.fileAccess.FileAccessUtils;
import com.radiant.fileAccess.path.domain.FileAccessPath;
import com.radiant.fileAccess.path.service.FileAccessPathService;
import com.radiant.schedule.PeriodicActivitiesRegistry;
import com.radiant.schedule.PeriodicActivity;
import com.radiant.util.HttpRangeUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

@Service
@Transactional
@ParametersAreNonnullByDefault
public class FileAccessServiceImpl implements FileAccessService, PeriodicActivity {
   private static final Logger LOG = LoggerFactory.getLogger(FileAccessServiceImpl.class);
   private static final long TEMP_FOLDER_MAX_SIZE_B = 10737418240L;
   private static final long TEMP_FOLDER_THRESHOLD_SIZE_B = 7516192768L;
   private static final long TEMP_FILE_LIFETIME_MIN_MLS = 600000L;
   private static final long UNKNOWN_SIZE = -1L;
   private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
   private static final String EXPOSE_HEADERS = (new StringJoiner(",")).add("Accept-Ranges").add("Content-Range").add("Content-Encoding").add("Content-Type").toString();
   private static final int PDF_FONT_SIZE = 14;
   private static final int PDF_TEXT_X = 10;
   private static final int PDF_TEXT_Y = 600;
   private static final String MERGED_DIRECTORY = "/tmp/";
   private static final String MERGED_PATTERN = "_directory_merged_";
   @Autowired
   private FileAccessPathService fileAccessPathService;
   @Autowired
   private PeriodicActivitiesRegistry periodicActivitiesRegistry;
   @Autowired
   private DataConnectorService dataConnectorService;

   public void performActivity(Object context) {
      FileAccessUtils.cleanFilePathCache();
      FileAccessPath tempFilesPath = this.fileAccessPathService.findByLogicalPath(Paths.get("temp"));
      File tempFolder = new File(tempFilesPath.getPhysicalPath());
      if (tempFolder.isDirectory() && tempFolder.exists() && FileUtils.sizeOfDirectory(tempFolder) > 10737418240L) {
         File[] tempFiles = tempFolder.listFiles();
         if (tempFiles != null) {
            List<File> files = (List)Arrays.stream(tempFiles).sorted(Comparator.comparing(File::lastModified)).collect(Collectors.toList());
            long minLifetimeLimit = System.currentTimeMillis() - 600000L;

            for(File file : files) {
               if (!FileUtils.isFileOlder(file, minLifetimeLimit)) {
                  LOG.info("Skip removing for newly modified temp files");
                  break;
               }

               try {
                  if (file.isDirectory()) {
                     FileUtils.deleteDirectory(file);
                  } else {
                     FileUtils.delete(file);
                  }

                  if (FileUtils.sizeOfDirectory(tempFolder) < 7516192768L) {
                     LOG.info("Folder size threshold is reached, stop removing");
                     break;
                  }
               } catch (IOException e) {
                  LOG.error("Failed to delete temp file {}", file, e);
               }
            }

            LOG.info("Tmp files deleted, remaining folder size: {}", FileUtils.sizeOfDirectory(tempFolder));
         }
      }

   }

   @PostConstruct
   private void init() {
      if (this.periodicActivitiesRegistry != null) {
         this.periodicActivitiesRegistry.addShortPeriodActivity(this, "Cleanup temporary documents folder", (Object)null);
      }

   }

   private FileAccessPath validateAndGetFileAccessPath(String logicalPath, String internalPath) {
      Path fileNamePath = Paths.get(internalPath).normalize().getFileName();
      if (fileNamePath == null) {
         throw new NoFileNameInPath(logicalPath);
      } else {
         List<DataConnector> allDc = this.dataConnectorService.getAllConnectors();
         return this.fileAccessPathService.findByLogicalPathAndConnector(Paths.get(logicalPath).normalize(), allDc);
      }
   }

   private void doRead(FileDataConnector connector, HttpServletResponse response, FileAccessPath fileAccessPath, String normalizedPath, @Nullable String httpRange, @Nullable String customIp, String fullLogicalPath) throws Exception {
      List<HttpRange> ranges;
      long initialOffset;
      int rangeSize;
      try {
         ranges = httpRange == null ? Collections.emptyList() : HttpRange.parseRanges(httpRange);
         initialOffset = ranges.size() == 0 ? 0L : ((HttpRange)ranges.get(0)).getRangeStart(0L);
         rangeSize = ranges.size() == 1 ? (int)(((HttpRange)ranges.get(0)).getRangeEnd(2147483647L) - initialOffset) : 0;
      } catch (IllegalArgumentException e) {
         HttpRangeUtil.sendRangeError(response, -1L);
         return;
      }

      normalizedPath = FileAccessUtils.replacePatterns(normalizedPath);
      SizedInputStream file = (SizedInputStream)connector.accept(new FileFetcherVisitor(fileAccessPath, normalizedPath, customIp, fullLogicalPath, initialOffset, rangeSize));

      try (InputStream input = file.getInputStream()) {
         Long total = file.getContentLength();
         response.setHeader("Accept-Ranges", "bytes");
         if (ranges.isEmpty()) {
            response.setHeader("Content-Length", total.toString());
            long copied = (long)StreamUtils.copy(input, response.getOutputStream());
            if (copied != total) {
               LOG.warn("File wasn't fully read");
            }
         } else if (ranges.size() == 1) {
            HttpRange range = (HttpRange)ranges.get(0);
            long start = range.getRangeStart(total);
            long end = range.getRangeEnd(total);
            response.setHeader("Content-Range", String.format("bytes %s-%s/%s", start, end, total));
            if (start > 0L || end < total) {
               response.setStatus(206);
            }

            StreamUtils.copyRange(input, response.getOutputStream(), start - file.getOffset(), end - file.getOffset());
         } else {
            if (!HttpRangeUtil.validateHttpRanges(ranges, total)) {
               HttpRangeUtil.sendRangeError(response, total);
            }

            response.setContentType("multipart/byteranges; boundary=MULTIPART_BYTERANGES");
            response.setStatus(206);
            ServletOutputStream sos = response.getOutputStream();
            long inputPos = 0L;

            for(HttpRange range : ranges) {
               sos.println();
               long start = range.getRangeStart(total);
               long end = range.getRangeEnd(total);
               sos.println("--MULTIPART_BYTERANGES");
               sos.println(String.format("Content-Range: bytes %s-%s/%s", start, end, total));
               inputPos = start + StreamUtils.copyRange(input, sos, start - inputPos - file.getOffset(), end - inputPos - file.getOffset());
            }

            sos.println();
            sos.println("--MULTIPART_BYTERANGES--");
         }
      } catch (IOException e) {
         LOG.error("Failed to read file", e);
         throw new RdsmIOException(e);
      }
   }

   @Transactional(
      propagation = Propagation.NEVER
   )
   public void read(String logicalPath, String internalPath, HttpServletRequest request, HttpServletResponse response) throws Exception {
      String pathFromCache = FileAccessUtils.getPathFromHash(internalPath);
      URI storedURI = null;
      if (pathFromCache != null) {
         LOG.warn("Got internalPath '{}' that is actually hash, found real internalPath '{}' in cache", internalPath, FileAccessUtils.replacePatterns(pathFromCache));
         if (!pathFromCache.startsWith("http:") && !pathFromCache.startsWith("https:")) {
            internalPath = pathFromCache;
         } else {
            String uriString = FileAccessUtils.replacePatterns(pathFromCache);

            try {
               storedURI = new URI(uriString);
               internalPath = storedURI.getPath();
            } catch (URISyntaxException e) {
               LOG.error("Failed to parse URL from cached path: {}", uriString, e);
               throw new WrongUrlException(uriString);
            }
         }
      }

      String httpRange = request.getHeader("Range");
      FileAccessPath fileAccessPath = this.validateAndGetFileAccessPath(logicalPath, internalPath);
      Path physicalPathToFile = storedURI != null ? Paths.get(storedURI.getPath()) : Paths.get(fileAccessPath.getPhysicalPath()).resolve(internalPath);
      FileDataConnector connector = (FileDataConnector)fileAccessPath.getConnector();
      String normalizedPath = storedURI != null ? storedURI.toString() : connector.normalize(physicalPathToFile);
      Path filePath = Paths.get(internalPath).getFileName();
      String fileName = filePath != null ? filePath.toString() : "";
      if (fileName.contains("_directory_merged_")) {
         try (FileInputStream in = new FileInputStream("/tmp/" + fileName)) {
            StreamUtils.copy(in, response.getOutputStream());
         } catch (IOException e) {
            throw new RdsmIOException(e);
         }
      } else {
         response.setHeader("Access-Control-Expose-Headers", EXPOSE_HEADERS);
         response.setHeader("Access-Control-Allow-Headers", "*");
         response.setHeader("Access-Control-Allow-Origin", "*");
         normalizedPath = FileAccessUtils.replacePatterns(normalizedPath);
         if (connector instanceof HttpFileDataConnector) {
            HttpFileDataConnector httpConnector = (HttpFileDataConnector)connector;
            httpConnector.accept(new HttpRequestProxyVisitor(request, response, fileAccessPath, normalizedPath));
         } else {
            this.doRead(connector, response, fileAccessPath, normalizedPath, httpRange, (String)request.getAttribute("CUSTOM_IP_ADDRESS"), Paths.get(logicalPath, internalPath).toString());
         }

      }
   }

   @Transactional(
      propagation = Propagation.NEVER
   )
   public List<String> merge(String logicalPath, String internalPath, HttpServletRequest request, String sort, int limit) throws Exception {
      FileAccessPath fileAccessPath = this.validateAndGetFileAccessPath(logicalPath, internalPath);
      Path physicalPathToFile = Paths.get(fileAccessPath.getPhysicalPath(), internalPath);
      FileDataConnector connector = (FileDataConnector)fileAccessPath.getConnector();
      String normalizedPath = FileAccessUtils.replacePatterns(connector.normalize(physicalPathToFile));
      List<String> result = new ArrayList();
      List<String> filePaths = (List)((List)connector.accept(new FileListFetcherVisitor(fileAccessPath, normalizedPath))).stream().filter((f) -> f.toLowerCase().endsWith(".pdf") || f.toLowerCase().endsWith(".jpg") || f.toLowerCase().endsWith(".tiff")).sorted((first, second) -> "desc".equalsIgnoreCase(sort) ? second.compareTo(first) : first.compareTo(second)).collect(Collectors.toList());
      Path directoryPath = Paths.get(normalizedPath).getFileName();
      String directoryName = directoryPath != null ? directoryPath.toString() : "";
      String docName = "";
      PDFMergerUtility mergerUtility = new PDFMergerUtility();
      limit = limit == 0 ? filePaths.size() : Math.min(limit, filePaths.size());
      Map<String, File> tempImageFiles = new HashMap();
      int startDocNum = 1;
      int filesInDoc = 0;
      int filesPassed = 0;
      PDDocument doc = new PDDocument();

      try {
         for(String filePath : filePaths) {
            filePath = FileAccessUtils.replacePatterns(filePath);
            SizedInputStream file = (SizedInputStream)connector.accept(new FileFetcherVisitor(fileAccessPath, filePath, (String)request.getAttribute("CUSTOM_IP_ADDRESS"), Paths.get(logicalPath, internalPath).toString()));
            if (filePath.toLowerCase().endsWith(".pdf")) {
               mergerUtility.appendDocument(doc, PDDocument.load(file.getInputStream()));
            } else {
               this.addImageInPdf(doc, filePath, this.saveImageInTempFiles(tempImageFiles, filePath, file.getInputStream()));
            }

            docName = String.format("%s(%s-%s)%s%s.pdf", directoryName, startDocNum, startDocNum + filesInDoc, "_directory_merged_", UUID.randomUUID());
            ++filesInDoc;
            ++filesPassed;
            if (filesInDoc == limit || filesPassed == filePaths.size()) {
               doc.save("/tmp/" + docName);
               doc.close();
               result.add(normalizedPath + "/" + docName);
               if (filesPassed != filePaths.size()) {
                  startDocNum += limit;
                  filesInDoc = 0;
                  doc = new PDDocument();
               }
            }
         }

         tempImageFiles.forEach((imageFileName, imageFile) -> {
            if (!imageFile.delete()) {
               LOG.error("Failed to delete image {}", imageFileName);
            }

         });
         return result;
      } catch (IOException e) {
         LOG.error("Failed to merge directory {}", normalizedPath, e);
         throw new RdsmIOException(e);
      }
   }

   private File saveImageInTempFiles(Map<String, File> tempImageFiles, String filePath, InputStream file) {
      File tempFile = new File("/tmp/" + UUID.randomUUID());

      try {
         FileUtils.copyInputStreamToFile(file, tempFile);
      } catch (Exception e) {
         LOG.error("Failed to save image {}", filePath, e);
      }

      tempImageFiles.put(filePath, tempFile);
      return tempFile;
   }

   private void addImageInPdf(PDDocument doc, String imageFileName, File imageFile) {
      try {
         doc.addPage(new PDPage());
         int pageCount = doc.getDocumentCatalog().getPages().getCount();
         PDPage page = doc.getPage(pageCount - 1);
         try (PDPageContentStream contents = new PDPageContentStream(doc, page, AppendMode.APPEND, true, false)) {
            try {
               PDImageXObject pdImage = PDImageXObject.createFromFileByContent(imageFile, doc);
               contents.drawImage(pdImage, 0.0F, 0.0F, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            } catch (Exception e) {
               contents.setFont(PDType1Font.HELVETICA_BOLD, 14.0F);
               contents.beginText();
               contents.newLineAtOffset(10.0F, 600.0F);
               contents.showText(String.format("Failed to add image %s into PDF file", imageFileName));
               contents.endText();
               LOG.error("Failed to add image {} into PDF file", imageFileName, e);
            }
         }
      } catch (Exception e) {
         LOG.error("Failed to add image {} into PDF file", imageFileName, e);
      }

   }
}
