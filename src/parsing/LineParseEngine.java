/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

/**
 *
 * @author Piotr
 */
public interface LineParseEngine {
    public String[] getUrlsOfLines();
    public String[] getNameOfLines();
    public String[] getCanonicalLines();
    public void parseLineDocument(String doc, String baseUrl);
}
