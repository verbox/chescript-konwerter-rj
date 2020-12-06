/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package output.generator;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import parsing.OneDirectionParsedSchedules;
import parsing.ParsedSchedule;
import schedules.CourseStop;
import schedules.ScheduleType;
import static utilities.StringUtilities.*;

/**
 *
 * @author Piotr
 */
public class SCDOutputGenerator {
    List<String> bufferedDifferencesLists = null;
    /** Wpieprza daną listę godzin do Stringa
     * 
     * @param filename
     * @param schedule
     * @return 
     */
    @Deprecated
    public String oneDay(List<CourseStop> cstops) throws NullPointerException{
        //dla pewności posortuj
        Collections.sort(cstops);
        //jeżeli cstops jest nullem albo jest puste - wyjeb się
        if (cstops == null && cstops.size()==0)
            throw new NullPointerException();
        //wyciągnij najwcześniejszą godzinę
        int earliest = cstops.get(0).getHour();
        //najpóźniejszą
        int latest = cstops.get(cstops.size()-1).getHour();
        //i aktualnie używaną
        int currentUseHour = earliest;
        //stringbuffer do budowania
        StringBuffer result = new StringBuffer(earliest+"|");
        //i jedziemy
        for(int i=0; i<cstops.size(); ++i) {
            //pobierz kolejny element listy
            CourseStop cs = cstops.get(i);
            //pobierz godzinę
            int hour = cs.getHour();
            //jeżeli jest inna niż aktualnie używana - doklej średnik, nową i pałkę
            if (hour != currentUseHour) {
                result.append(";").append(hour).append("|");
                currentUseHour = hour;
                //doklej minuty
                result.append(cs.getMinute());
            } else {
                //ja pieprzę, takie numery trzeba stosować, aby przed pierwszą minutą nie było zonka:
                if (i!=0) result.append(" ");
                result.append(cs.getMinute());
            }

            //jeżeli opis jest inny niż "", to też go doklej
            if (!cs.getDescription().equals(""))
                result.append("?").append(cs.getDescription());
        }
        //i tyle, zwróć co trzeba
        return new String(result);
    }
    
    /** Wpieprza listę kursów do stringa.
     * Na zasadzie np. 4:05;5;5?$;5;5;5 itd. (różnice)
     * @param cstops
     * @return
     * @throws NullPointerException 
     */
    public String newOneDay(List<CourseStop> cstops) throws NullPointerException{
        //dla pewności posortuj
        Collections.sort(cstops);
        //jeżeli cstops jest nullem - wyjeb się
        if (cstops == null)
            throw new NullPointerException();
        //jak size = 0 - tylko znak -
        if (cstops.size()==0)
            return "-";
        //stringbuffer do budowania
        StringBuffer result = new StringBuffer("");
        //wrzuć od razu pierwszą godzinę z ewentualnym opisem
        CourseStop current = cstops.get(0);
        result.append(current.getHour()).append(':').append(current.getMinute());
        if (!current.getDescription().equals("")) {
            result.append('?').append(current.getDescription());
        }
        //STOP! Update 170113
        //buduj sobie, jak Bóg nakazał
        StringBuffer differencesBuilder = new StringBuffer("");
        int previousDifference = 0;
        String previousDescription = "";
        //kolejne elementy jako różnice
        for(int i=1; i<cstops.size(); ++i) {
            //weź następny
            CourseStop next = cstops.get(i);
            //policz różnicę - wyjdzie na minus pierwszego, więc z minusem
            int difference = -CourseStop.diferrence(current, next);
            String description = next.getDescription();
            //TODO kolejne mieszanie
            //jeżeli różnica jest taka sama, jak poprzedn
            //wpisz średnik i różnicę
            
            differencesBuilder.append(';');
            if (!(previousDifference==difference && previousDescription.equals(description)))
            {
                differencesBuilder.append(difference);
                //jak next ma opis - to też go dopisz
                if (!description.equals("")) {
                    differencesBuilder.append('?').append(description);
                }
            }
            //current = next
            previousDifference=difference;
            previousDescription=description;
            current = next;
        }
        String differencesString = differencesBuilder.length() == 0 ? "" : differencesBuilder.substring(1);
        //znajdź takiego stringa na liście różnic
        int findedIndex = bufferedDifferencesLists.indexOf(differencesString);
        //jak znaleziono - to append(;+"_"+findedIndex);
        if (findedIndex!=-1) {
            result.append(";").append(findedIndex);
        } else {
            //a jak nie - to najpierw dodaj do listy, znajdź i dowal indeksa
            bufferedDifferencesLists.add(differencesString);
            result.append(";").append(bufferedDifferencesLists.size()-1);
        }
        //koniec
        return result.toString();        
    }

    
    /** Zwraca literkę odpowiadającą danemu typu rozkładów
     * 
     * @param st
     * @return 
     */
    public char getCharOfScheduleType(ScheduleType st) {
        if (st==ScheduleType.WORKING_DAYS) return 'R';
        if (st==ScheduleType.SUMMER_DAYS) return 'W';
        if (st==ScheduleType.SATURDAY_DAYS) return 'S';
        return 'N';
    }
    
    //zamienia tablicę na listę linii, listę przystanków itp.
    public String listOfElements(String[] lines) {
        StringBuffer sb = new StringBuffer(lines[0]);
        //wrzucaj - średnik a potem nazwę linii
        for(int i=1; i<lines.length; i++) {
            sb.append(";"+lines[i]);
        }
        return sb.toString();
    }
    
    //generuje plik z listą linii
    public boolean generateListOfLines(String filename, String[] lines) {
        try {
            File file = new File(filename);
            if (file.exists())
                file.delete();
            saveStringToFile(file,listOfElements(lines));
        }
         catch (IOException ex) {
                System.out.println("Błąd z zapisywaniem pliku przy generowaniu rozkładów, "+ex.getMessage());
                return false;
            } 
         return true;
    }
    
    /** Generuje plik z kierunkami.
     * UWAGA!!! FILENAME TO TYLKO NAZWA PLIKU.
     * 
     * @param filename
     * @param directions
     * @return 
     */
    public boolean generateListOfDirections(String filename, String line, String[] directions) {
        try {
            File directory = new File(line+"\\");
            if (!directory.exists())
                directory.mkdirs();
            File file = new File(line+"\\"+filename);
            if (file.exists())
                file.delete();
            saveStringToFile(file,listOfElements(directions));
        }
         catch (IOException ex) {
                System.out.println("Błąd z zapisywaniem pliku przy generowaniu kierunków, "+ex.getMessage());
                return false;
            } 
         return true;
    }
    
    /** Generuje plik z listą przystanków dla danego kierunku.
     * Argumentem jest lista sparsowanych przystanków.
     * @param filename
     * @param directions
     * @return 
     */
    public boolean generateListOfStops(String filename, OneDirectionParsedSchedules odps) {
         try {
            File file = new File(filename);
            if (file.exists())
                file.delete();
            //utwórz tablicę nazw przystanków
            String[] stops = new String[odps.getPschedules().size()];
            //boję się użyć foreacha, więc naokoło
            for(int i=0; i<stops.length; ++i) {
                stops[i] = odps.getPschedules().get(i).getStop();
            }
            //teraz tylko zapis
            saveStringToFile(file,listOfElements(stops));
        }
         catch (IOException ex) {
                System.out.println("Błąd z zapisywaniem pliku przy generowaniu listy przystanków, "+ex.getMessage());
                return false;
            } 
         return true;
    }
    
    public boolean generateLegend(String filename, OneDirectionParsedSchedules odps) {
        try {
            File file = new File(filename);
            if (file.exists())
                file.delete();
            //wyciągnij pierwszego przystanka
            ParsedSchedule ps = odps.getPschedules().get(0);
            saveStringToFile(file,ps.getLegend());
        }
         catch (IOException ex) {
                System.out.println("Błąd z zapisywaniem pliku przy generowaniu listy przystanków, "+ex.getMessage());
                return false;
            } 
         return true;
    }
    
    /** Generuje plik z samymi typami rozkładów. 
     * Wyciąga je z pierwszego przystanku.
     * @param filename
     * @param odps
     * @return 
     */
    public boolean generateTypesOfSchedule(String filename, OneDirectionParsedSchedules odps) {
        try {
            File file = new File(filename);
            if (file.exists())
                file.delete();
            //utwórz stringa wynikowego
            String types = "";
            //boję się użyć foreacha, więc naokoło
            //wyciągnij tablicę typów
            ScheduleType[] typesArray = odps.getPschedules().get(0).getScheduleTypes();
            //i dopisz
            for(int i=0; i<typesArray.length; ++i) {
                types+=getCharOfScheduleType(typesArray[i]);
            }
            //teraz tylko zapis
            saveStringToFile(file,types);
        }
         catch (IOException ex) {
                System.out.println("Błąd z zapisywaniem pliku przy generowaniu listy typów rozkładów, "+ex.getMessage());
                return false;
            } 
         return true;
    }
    
    /** Generuje rozkład w stylu
     * LITERKA_TYPU_ROZKŁADU
     * 4|53;5|53
     * ###
     * LITERKA_TYPU_ROZKŁADU
     * itp.
     * [ostatnia linijka]
     * 4|53
     * ###
     * #__#
     * @param psch
     * @return 
     */
    public String generateStopSchedule(ParsedSchedule psch) {
        StringBuffer sb = new StringBuffer("");
        String prevDay = "";
        String act;
        for(int i=0; i<psch.getScheduleTypes().length; ++i) {
            //dodaj literkę typu rozkładu i enter
            act = newOneDay(psch.getSchedules().get(i));
            //sb.append(getCharOfScheduleType(psch.getScheduleTypes()[i]));
            //sprawdź, czy wygenerowany rozkład (act) nie jest taki sam jak (prev)
            //dodaj tylko jak są różne!
            if (!act.equals(prevDay))
                sb.append(act);
            //uaktualnij prevDay
            prevDay = act;
            //dodaj @
            sb.append("@");
        }
        //dodaj nową linię
        //sb.append("\n");
        return sb.toString();
    }
    
    /** Generuje cały kierunek - przystanki oddzielone @\n
     * 
     * @param odps
     * @return 
     */
    public String generateOneDirection(OneDirectionParsedSchedules odps) {
        StringBuffer sb = new StringBuffer("$");
        //lista przystanków
        //utwórz tablicę nazw przystanków
            String[] stops = new String[odps.getPschedules().size()];
            //boję się użyć foreacha, więc naokoło
            for(int i=0; i<stops.length; ++i) {
                stops[i] = odps.getPschedules().get(i).getStop();
            }
        //dodaj do sb
        sb.append(listOfElements(stops)).append("$");
        /*
         * RODZAJE ROZKŁADÓW
         */
        //utwórz stringa wynikowego
            String types = "";
            //boję się użyć foreacha, więc naokoło
            //wyciągnij tablicę typów
            ScheduleType[] typesArray = odps.getPschedules().get(0).getScheduleTypes();
            //i dopisz
            for(int i=0; i<typesArray.length; ++i) {
                types+=getCharOfScheduleType(typesArray[i]);
            }
        //dodaj do sb
        sb.append(types).append("=");
        //dodaj legendę
        //wyciągnij pierwszego przystanka
            ParsedSchedule ps = odps.getPschedules().get(0);
        sb.append(ps.getLegend()).append("|");
        //i już rozkładzik
        for(int i=0; i<odps.getPschedules().size(); ++i) {
            sb.append(generateStopSchedule(odps.getPschedules().get(i)));
        }
        //dodaj##
        
        return sb.toString();
    }
    
    public boolean generateStringToFile(String filename, String content) {
        try {
            File file = new File(filename);
            if (file.exists())
                file.delete();
            saveStringToFile(file,content);
            return true;
        } catch(IOException ex) {
            System.out.println("Błąd z generowaniem pliku "+filename+", "+ex.getMessage());
            return false;
        }
    }
    
    /** 
     * Razem z plikiem o przystankach i legendą
     * @param baseDir
     * @param line
     * @param direction_id
     * @param odps 
     */
    public void generateFilesOneDirection(String baseDir, String line, String direction_id, OneDirectionParsedSchedules odps) {
        //wygeneruj co się da
        generateListOfStops(baseDir+"\\"+direction_id+"_stops.scd", odps);
        generateLegend(baseDir+"\\"+direction_id+"_legend.scd", odps);
        generateStringToFile(baseDir+"\\"+direction_id+".scds", generateOneDirection(odps));
    }
    
    /** Wersja "wszystkie kierunki w jednym pliku"
     * 
     * @param baseDir
     * @param line
     * @param lodps 
     */
    public void generateOneLine(String baseDir, String line, List<OneDirectionParsedSchedules> lodps) {
        //nowa lista różnic
        bufferedDifferencesLists = new LinkedList<>();
        //na rozkłady
        StringBuffer oneDirectSchedules = new StringBuffer("");
        //na listę kierunków
        String[] directions = new String[lodps.size()];
        for(int i=0; i<lodps.size();++i) {
            oneDirectSchedules.append(generateOneDirection(lodps.get(i))).append("");
            //wyciągnij kierunek
            directions[i] = lodps.get(i).getDirection();
        }
        String allSchedules = oneDirectSchedules.toString();
        allSchedules = allSchedules.replaceAll("<TD COLSPAN=[0-9]>","");
        generateStringToFile(line+"_SCH.scds",generateDifferencesList()+"$"+listOfElements(directions)+allSchedules);
    }
    
    public String generateDifferencesList() {
        StringBuffer result = new StringBuffer("");
        for(int i=0; i<bufferedDifferencesLists.size(); ++i) {
            result.append(bufferedDifferencesLists.get(i) +"\\");
        }
        if (result.length()==0) return "";
        return result.substring(0,result.length()-1);
    }
}
