/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedules;

/** Klasa reprezentująca przystanek.
 *
 * @author Piotr
 */
public class Stop {
    private Direction direction;
    private String name;
    private int number;
    
    public Direction getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public Stop(Direction direction, String name, int number) {
        this.direction = direction;
        this.name = name;
        this.number = number;
    }
    
    public ScheduleStop getScheduleStop(ScheduleType st) {
        return null;
    }
    
    
}
