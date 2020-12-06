/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nschedules;

/**
 *
 * @author Piotr
 */
public class StopDifference {
    int interval;
    String desc;

    public StopDifference(int interval, String desc) {
        this.interval = interval;
        this.desc = desc;
    }
    
    public int getInterval() {
        return interval;
    }

    public String getDesc() {
        return desc;
    }
    
    public CourseStop getAddedCourseStop(CourseStop basic) {
        //todo change
        return null;
    }
}
