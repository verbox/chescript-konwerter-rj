/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedules;

/** Klasa reprezentująca czas podstawowego kursu.
 *
 * @author Piotr
 */
public class CourseStop implements Comparable<CourseStop> {
    private byte hour;
    private byte minute;

    public CourseStop(byte hour, byte minute, String description) {
        this.hour = hour;
        this.minute = minute;
        this.description = description;
    }
    
    public CourseStop(byte hour, byte minute) {
        this(hour,minute,"");
    }
    
    public static int diferrence(CourseStop first, CourseStop second) {
        int valueFirst = first.getMinute()+first.getHour()*60;
        int valueSecond = second.getMinute()+second.getHour()*60;
        return valueFirst-valueSecond;
    }
    
    private String description;

    public String getDescription() {
        return description;
    }

    public byte getHour() {
        return hour;
    }

    public byte getMinute() {
        return minute;
    }
    
    @Override
    public String toString() {
        return hour+":"+minute+description;
    }

    @Override
    public int compareTo(CourseStop o) {
        //if (this.hour==23 && o.hour==0) return -1;
        //if (this.hour==0 && o.hour==23) return 1;
        if (this.hour<o.hour) return -1;
        if (this.hour>o.hour) return 1;
        if (this.minute<o.minute) return -1;
        if (this.minute>o.minute) return 1;
        return 0;
    }
}
