/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

/**
 *
 * @author Piotr
 */
public class ScheduleParser {
    private ScheduleParseEngine parsingEngine;

    public ScheduleParser(ScheduleParseEngine parsingEngine) {
        this.parsingEngine = parsingEngine;
    }
    
    public void setParsingEngine(ScheduleParseEngine parsingEngine) {
        this.parsingEngine = parsingEngine;
    }
    
    public ParsedSchedule parseDocument(String doc) {
        parsingEngine.parseString(doc);
        return parsingEngine.getParsedSchedule();
    }
}
