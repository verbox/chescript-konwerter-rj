/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedules;

/** Klasa reprezentująca rozkład.
 *
 * @author Piotr
 */
public class ScheduleStop {
    private char type; //Robocze, Wakacyjne, Soboty, Niedzieleiświęta
    private Stop stop;

    public Stop getStop() {
        return stop;
    }

    public char getType() {
        return type;
    }
    private CourseStop[] courseStop;

    public void setCourseStop(CourseStop[] courseStop) {
        this.courseStop = courseStop;
    }

    public ScheduleStop(char type, CourseStop[] courseStop, Stop stop) {
        this.type = type;
        this.stop = stop;
        this.courseStop = courseStop;
    }
    
    public ScheduleStop(char type, Stop stop) {
        this(type,null,stop);
    }
    
    public CourseStop[] getCourseStops() {
        //taki trochę singleton
        return courseStop;  
    }
}
