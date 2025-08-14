package com.radiant.fileAccess.service;

import com.radiant.fileAccess.dto.FileAccessProgramDto;
import java.util.List;

public interface FileAccessProgramService {
   FileAccessProgramDto get(Long id);

   List<FileAccessProgramDto> getAll();

   FileAccessProgramDto patch(Long id, FileAccessProgramDto request);
}
