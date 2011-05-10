package hudson.plugins.violations.types.jslint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.plugins.violations.ViolationsParser;
import hudson.plugins.violations.model.FullBuildModel;
import hudson.plugins.violations.model.FullFileModel;
import hudson.plugins.violations.model.Severity;
import hudson.plugins.violations.model.Violation;
import hudson.plugins.violations.util.AbsoluteFileFinder;

/**
 * Parser for parsing JsLint reports.
 *
 * The parser only supports JsLint report that has the line output format
 * filename:lineNumber:character:violationId:message 
 *
 * @author Eric Nyaben
 */
public class JsLintParser implements ViolationsParser {
	
	static final String TYPE_NAME = "jslint";

    /** Regex pattern for the JsLint errors. */
    private final transient Pattern pattern;
    private transient AbsoluteFileFinder absoluteFileFinder = new AbsoluteFileFinder(); 

    /**
     * Constructor - create the pattern.
     */
    public JsLintParser() {
        pattern = Pattern.compile("(.*):(\\d+):(\\d+):\\[(.*)\\](.*)");
    }

    /** {@inheritDoc} */
    public void parse( FullBuildModel model, File projectPath, String fileName,
        String[] sourcePaths) throws IOException {
        
    	BufferedReader reader = null;
        
    	absoluteFileFinder.addSourcePath(projectPath.getAbsolutePath());
    	//absoluteFileFinder.addSourcePaths(sourcePaths);
        
        try {
            reader = new BufferedReader(
                new FileReader(new File(projectPath, fileName)));
            String line = reader.readLine();
            while (line != null) {
                parseLine(model, line, projectPath);
                line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Parses a JsLint line and adding a violation if regex
     * @param model build model to add violations to
     * @param line the line in the file.
     * @param projectPath the path to use to resolve the file.
     */
    public void parseLine(FullBuildModel model, String line, File projectPath) {
        JsLintViolation jsLintViolation = getJsLintViolation(line);

        if (jsLintViolation != null) {

            Violation violation = new Violation();
            violation.setType("jslint");
            violation.setLine(jsLintViolation.getLineStr());
            violation.setMessage(jsLintViolation.getMessage());
            violation.setSource(jsLintViolation.getViolationId());
            setServerityLevel(violation, jsLintViolation.getViolationId());

            FullFileModel fileModel = getFileModel(model, 
            		jsLintViolation.getFileName(), 
            		absoluteFileFinder.getFileForName(jsLintViolation.getFileName()));
            fileModel.addViolation(violation);
        }
    }
    
    private FullFileModel getFileModel(FullBuildModel model, String name, File sourceFile) {
        FullFileModel fileModel = model.getFileModel(name);
        File other = fileModel.getSourceFile();

        if (sourceFile == null
            || ((other != null) && (
                    other.equals(sourceFile)
                    || other.exists()))) {
            return fileModel;
        }
        
        fileModel.setSourceFile(sourceFile);
        fileModel.setLastModified(sourceFile.lastModified());
        return fileModel;
    }
    

    /**
     * Returns a jslint violation (if it is one)
     * @param line a line from the JsLint parseable report
     * @return a JsLintViolation if the line contains one; null otherwise
     */
    JsLintViolation getJsLintViolation(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find() && matcher.groupCount() == 5) {
            return new JsLintViolation(matcher);
        }
        return null;
    }

    /**
     * Returns the Severity level as an int from the JsLint message type.
     *
     * The different message types are:
     * (C) convention, for programming standard violation
     * (R) refactor, for bad code smell
     * (W) warning, for javascript specific problems
     * (E) error, for much probably bugs in the code
     * (F) fatal, if an error occured which prevented jslint from doing
     *     further processing.
     *
     * @param messageType the type of JsLint message
     * @return an int is matched to the message type.
     */
    private void setServerityLevel(Violation violation, String messageType) {

        switch (messageType.charAt(0)) {
            case 'C':
                violation.setSeverity(Severity.LOW);
                violation.setSeverityLevel(Severity.LOW_VALUE);
                break;
            case 'R':
                violation.setSeverity(Severity.MEDIUM_LOW);
                violation.setSeverityLevel(Severity.MEDIUM_LOW_VALUE);
                break;
            case 'W':
                violation.setSeverity(Severity.MEDIUM_LOW);
                violation.setSeverityLevel(Severity.MEDIUM_LOW_VALUE);
                break;
            default:
                violation.setSeverity(Severity.MEDIUM);
                violation.setSeverityLevel(Severity.MEDIUM_VALUE);
                break;
            case 'E':
                violation.setSeverity(Severity.MEDIUM_HIGH);
                violation.setSeverityLevel(Severity.MEDIUM_HIGH_VALUE);
                break;
            case 'F':
                violation.setSeverity(Severity.HIGH);
                violation.setSeverityLevel(Severity.HIGH_VALUE);
                break;
        }
    }
    
    class JsLintViolation {
        private final transient String lineStr;
        private final transient String charachter;
        private final transient String message;
        private final transient String fileName;
        private final transient String violationId;

        public JsLintViolation(Matcher matcher) {
            if (matcher.groupCount() < 5) {
                throw new IllegalArgumentException(
                    "The Regex matcher could not find enough information");
            }
            fileName = matcher.group(1);
            lineStr = matcher.group(2);
            charachter = matcher.group(3);
            violationId = matcher.group(4);
            message = matcher.group(5);
        }

        public String getLineStr() {
            return lineStr;
        }

        public String getMessage() {
            return message;
        }

        public String getFileName() {
            return fileName;
        }

        public String getViolationId() {
            return violationId;
        }

        public String getCharachter() {
            return charachter;
        }
    }
}

