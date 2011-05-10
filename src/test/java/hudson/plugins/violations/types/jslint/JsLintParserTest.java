package hudson.plugins.violations.types.jslint;

import static org.junit.Assert.*;
import hudson.plugins.violations.model.FullBuildModel;
import hudson.plugins.violations.model.Severity;
import hudson.plugins.violations.model.Violation;
import hudson.plugins.violations.types.jslint.JsLintParser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.junit.Test;

public class JsLintParserTest {
    
    private FullBuildModel getFullBuildModel(String filename) throws IOException {
        URL url = getClass().getResource(filename);
        File xmlFile;
        try {
            xmlFile = new File(url.toURI());
        } catch(URISyntaxException e) {
            xmlFile = new File(url.getPath());
        }
        
        JsLintParser parser = new JsLintParser();
        FullBuildModel model = new FullBuildModel();
        parser.parse(model, xmlFile.getParentFile(), xmlFile.getName(), null);
        model.cleanup();
        return model;
    }

    @Test
    public void testParseWithSingleFile() throws Exception {
        FullBuildModel model = getFullBuildModel("jslintReport.log");

        // check number of violations and number of files
        //assertEquals(51, model.getCountNumber(JsLintParser.TYPE_NAME));
        //assertEquals(1, model.getFileModelMap().size());
        
        assertPrototype(model);
    }
    
    /*@Test
    public void testParseWithMultipleFile() throws Exception {
        FullBuildModel model = getFullBuildModel("multi.xml");

        assertEquals(102, model.getCountNumber(JsLintParser.TYPE_NAME));
        assertEquals(2, model.getFileModelMap().size());
        
        assertScriptaculous(model);
        assertPrototype(model);
    }*/
    
    private void assertPrototype(FullBuildModel model) {
        Iterator<Violation> iterator = model.getFileModel("trunk/src/main/webapp/test/js/jquery-1.2.5.js").getTypeMap().get(JsLintParser.TYPE_NAME).iterator();
        
        // check the first two violations
        Violation v = iterator.next();
        assertEquals("convention, for programming standard violation", v.getPopupMessage());
        assertEquals(14, v.getLine());
        assertEquals(Severity.LOW, v.getSeverity());
        //assertEquals("1',", v.getSource());
        v = iterator.next();
        assertEquals("refactor, for bad code smell", v.getPopupMessage());
        assertEquals(15, v.getLine());
        assertEquals(Severity.MEDIUM_LOW, v.getSeverity());
        //assertEquals("  Browser: (function(){", v.getSource());
        
        // check the last violation
        while (iterator.hasNext()) {
            v = iterator.next();
        }
        assertEquals("fatal222, if an error occured", v.getPopupMessage());
        assertEquals(19, v.getLine());
        assertEquals(Severity.HIGH, v.getSeverity());
        //assertEquals("", v.getSource());
    }
    
    private void assertScriptaculous(FullBuildModel model) {
        Iterator<Violation> iterator = model.getFileModel("duckworth/hudson-jslint-freestyle/src/scriptaculous.js").getTypeMap().get(JsLintParser.TYPE_NAME).iterator();
        
        // check the first two violations
        Violation v = iterator.next();
        assertEquals("convention, for programming standard violation", v.getPopupMessage());
        assertEquals(14, v.getLine());
        assertEquals(Severity.LOW, v.getSeverity());
        //assertEquals("1',", v.getSource());
        v = iterator.next();
        assertEquals("refactor, for bad code smell", v.getPopupMessage());
        assertEquals(15, v.getLine());
        assertEquals(Severity.MEDIUM_LOW, v.getSeverity());
        //assertEquals("  Browser: (function(){", v.getSource());
        
        // check the last violation
        while (iterator.hasNext()) {
            v = iterator.next();
        }
        assertEquals("fatal, if an error occured", v.getPopupMessage());
        assertEquals(18, v.getLine());
        assertEquals(Severity.HIGH, v.getSeverity());
        //assertEquals("", v.getSource());
        
    }
}