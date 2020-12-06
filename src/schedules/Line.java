/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schedules;
/** Klasa reprezentująca pojedynczą linię.
 *
 * @author Piotr
 */
public class Line {
    private String number;

    /** Konstruktor tworzący linię (na podstawie numeru linii)
     * 
     * @param number 
     */
    public Line(String number) {
        this.number = number;
    }

    
    
    /** Zwraca numer
     * 
     * @return
     */
    public String getNumber() {
        return number;
    }
    
    public Direction[] getDirections() {
        //wykorzystaj obiekt generatora rozkładów
        return null;
    }

    
    
}
