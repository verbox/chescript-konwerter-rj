/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

/**
 *
 * @author Piotr
 */
public interface ScheduleParseEngine {
    public void parseString(String document);
    public ParsedSchedule getParsedSchedule();
}
