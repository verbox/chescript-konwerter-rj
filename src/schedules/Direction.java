/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package schedules;

/** Klasa reprezentująca kierunek
 *
 * @author Piotr
 */
public class Direction {
    private Line line;
    private String name;
    private int number;
    
    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public Direction(Line line, String name, int n) {
        this.line = line;
        this.number = n;
        this.name = name;
    }

    public Line getLine() {
        return line;
    }
    
    public Stop[] getStops() {
        return null;
    }
    
    public String getLegend() {
        return null;
    }
    

}
