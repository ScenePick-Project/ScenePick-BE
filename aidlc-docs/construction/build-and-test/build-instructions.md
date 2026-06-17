# Build Instructions

## Prerequisites
- **Build Tool**: Gradle 8.x
- **Java Version**: Java 21 (LTS)
- **Dependencies**: All dependencies managed by Gradle (see build.gradle)
- **Database**: Oracle Database (for integration testing)
- **System Requirements**: 
  - OS: Windows/Linux/macOS
  - Memory: 4GB RAM minimum
  - Disk Space: 2GB free space

## Build Steps

### 1. Install Dependencies
```bash
# Gradle will automatically download dependencies
./gradlew clean
```

**Note**: First run may take several minutes to download dependencies.

### 2. Configure Environment
```bash
# Set Java 21 as active JDK
# Windows (PowerShell):
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"

# Linux/macOS:
export JAVA_HOME=/path/to/jdk-21
```

**Database Configuration** (for integration tests):
- Configure database connection in `src/main/resources/application.properties` or `application.yml`
- Ensure Oracle database is running and accessible

### 3. Compile Code
```bash
# Compile all source code
./gradlew compileJava

# Expected output:
# BUILD SUCCESSFUL in Xs
```

### 4. Build All Units
```bash
# Build entire project (compile + package)
./gradlew build

# Skip tests during build (if needed):
./gradlew build -x test
```

### 5. Verify Build Success
- **Expected Output**: 
  ```
  BUILD SUCCESSFUL in Xs
  X actionable tasks: X executed
  ```
- **Build Artifacts**: 
  - JAR file: `build/libs/scene-pick-be-0.0.1-SNAPSHOT.jar`
  - Class files: `build/classes/java/main/`
- **Common Warnings**: 
  - Deprecation warnings are acceptable
  - Checkstyle warnings should be reviewed but don't block build

## Verify ReviewLike Feature Compilation

### Check Generated Classes
```bash
# Verify ReviewLike classes compiled successfully
ls build/classes/java/main/com/project/scenepickbe/review/vo/ReviewLikeVo.class
ls build/classes/java/main/com/project/scenepickbe/review/dao/ReviewLikeDao.class
ls build/classes/java/main/com/project/scenepickbe/review/service/ReviewLikeCommandService.class
ls build/classes/java/main/com/project/scenepickbe/review/controller/ReviewLikeController.class
```

All files should exist if compilation was successful.

## Troubleshooting

### Build Fails with Dependency Errors
- **Cause**: Network issues, repository unavailable, or corrupted Gradle cache
- **Solution**: 
  1. Check internet connection
  2. Clear Gradle cache: `./gradlew clean --refresh-dependencies`
  3. Retry build: `./gradlew build`

### Build Fails with Compilation Errors
- **Cause**: Syntax errors, missing imports, or type mismatches
- **Solution**: 
  1. Review error messages in console output
  2. Check file: `build/reports/compilation/compileJava.txt`
  3. Fix reported errors in source files
  4. Common issues:
     - Missing imports: Add required import statements
     - Type mismatches: Verify method signatures match interfaces
     - Lombok not working: Ensure Lombok plugin installed in IDE

### Build Fails with Checkstyle Violations
- **Cause**: Code doesn't follow Naver coding conventions
- **Solution**: 
  1. Review Checkstyle report: `build/reports/checkstyle/main.html`
  2. Fix violations according to `rule-config/naver-checkstyle-rules.xml`
  3. Common violations:
     - Indentation issues
     - Missing Javadoc comments
     - Line length exceeds 120 characters
     - Import order incorrect

### MyBatis Mapper Errors
- **Cause**: SQL syntax errors or mapper configuration issues
- **Solution**: 
  1. Verify ReviewLikeMapper.xml syntax
  2. Verify ReviewMapper.xml modifications
  3. Check namespace matches DAO interface fully qualified name
  4. Ensure resultType aliases are registered (@Alias annotation)

## Build Verification Checklist

- [ ] `./gradlew clean` completes successfully
- [ ] `./gradlew compileJava` completes without errors
- [ ] `./gradlew build` completes successfully
- [ ] JAR file created in `build/libs/`
- [ ] All ReviewLike classes compiled (5 new classes)
- [ ] No compilation errors in console output
- [ ] Checkstyle violations reviewed and acceptable

## Next Steps

After successful build:
1. Proceed to Unit Test Execution
2. Run Integration Tests
3. Verify API endpoints with Swagger UI

