/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

/**
 *
 * @author Piotr
 */
public class LineParser {
    private LineParseEngine engine;

    public LineParser(LineParseEngine engine, String parsingDocument, String baseUrl) {
        this.engine = engine;
        this.engine.parseLineDocument(parsingDocument, baseUrl);
    }
    
    public String[] getLines() {
        return this.engine.getNameOfLines();
    }
    
    public String[] getUrlLines() {
        return this.engine.getUrlsOfLines();
    }
    
    public String[] getCanonicalLines() {
        return this.engine.getCanonicalLines();
    }
    
    
}
