/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import schedules.*;
import schedules.CourseStop;
/**
 *
 * @author Piotr
 */
public class ParsedSchedule {
    private String line, stop, direction, legend;
    //tablica na rozkłady
    private ArrayList<List<CourseStop>> schedules;

    public ArrayList<List<CourseStop>> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<List<CourseStop>> schedules) {
        this.schedules = schedules;
    }
    private ScheduleType[] scheduleTypes;
    
    public ParsedSchedule() {
        schedules = new ArrayList< List<CourseStop> >();
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

    public void setLine(String line) {
        this.line = line;
    }

    /** Ustawianie typów rozkładów.
     * Pociąga za sobą utworzenie nowej tablicy czasów!
     * @param scheduleTypes 
     */
    public void setScheduleTypes(ScheduleType[] scheduleTypes) {
        this.scheduleTypes = scheduleTypes;
        this.schedules = new ArrayList<List<CourseStop>>(scheduleTypes.length);
        for(int i=0; i<scheduleTypes.length; ++i) {
            schedules.add(new LinkedList<CourseStop>());
        }
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    
    public String getLine() {
        return line;
    }
    public String getStop() {
        return stop;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public ScheduleType[] getScheduleTypes() {
        return scheduleTypes;
    }
    
    public String getLegend() {
        return legend;
    }
    
    public List<CourseStop> getCourseStop(ScheduleType st) {
        return null;
    }
    
    public List<CourseStop> getCourseStop(int index) {
        return schedules.get(index);
    }

    @Override
    public String toString() {
        return "ParsedSchedule{" + "line=" + line + ", stop=" + stop + ", direction=" + direction + ", legend=" + legend + ", schedules=" + schedules + ", scheduleTypes=" + scheduleTypes + '}';
    }
    
    
}
