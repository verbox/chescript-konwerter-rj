/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

import java.util.List;

/**
 *
 * @author Piotr
 */
public interface LineScheduleParserEngine {
    public void parseDocuments(String line, String canLine, String url);
    public String getLine();
    public String[] getDirections();
    public List<OneDirectionParsedSchedules> getParsedSchedules();
}
