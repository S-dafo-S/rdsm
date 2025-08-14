package com.radiant.fileAccess;

import com.radiant.fileAccess.domain.FileAccessProgramBase;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
   name = "file_access_program",
   uniqueConstraints = {@UniqueConstraint(
   name = "file_access_program_name_uniq",
   columnNames = {"name"}
)}
)
public class GddsFileAccessProgram extends FileAccessProgramBase {
   public static final String FILE_ACCESS_PROGRAM_NAME_UNIQ_CONSTRAINT = "file_access_program_name_uniq";
}
