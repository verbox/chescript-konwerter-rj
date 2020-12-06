/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

import java.util.LinkedList;
import java.util.List;

/** Zbiór rozkładów dla jednego z kierunków.
 * Piszę tak, żebym się nie zamotał z typami generycznymi w listach
 * @author Piotr
 */
public class OneDirectionParsedSchedules {
    //z lenistwa
    private List<ParsedSchedule> pschedules;

    public void setPschedules(List<ParsedSchedule> pschedules) {
        this.pschedules = pschedules;
    }

    public List<ParsedSchedule> getPschedules() {
        return pschedules;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public OneDirectionParsedSchedules(String direction, String line) {
        this.direction = direction;
        this.line = line;
    }
    private String direction;
    private String line;
}
