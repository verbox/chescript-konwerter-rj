/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import schedules.CourseStop;
import schedules.ScheduleType;
import static utilities.StringUtilities.*;

/** SUPERKLASA ATAKUJE
 *
 * @author Piotr
 */
public class HTMLBruteForceParseEngine implements ScheduleParseEngine, LineParseEngine, LineScheduleParserEngine{
    private ParsedSchedule sch;
    private String[] lines;
    private String[] urlLines;
    private String[] canonicalLines;
    final String MZD_PRZ = "http://mzd.czest.pl/images/rozklady/rozklady/";
    //final String MZD_PRZ = "file:///C:/040713/";
    /** 
     * Kupa roboty
     * @param document 
     */
    @Override
    public void parseString(String document) {
        //utwórz nowy sparsowany dokument
        sch = new ParsedSchedule();
        //TODO kolejna proteza koniecpolska - na rozkłady nocnych linii
        //w ogóle nie zaglądać
        if (document.contains("NOCNY - kursuje CODZIENNIE") ||
                document.contains("Linia Specjalna -  Kursuje tylko w dniu Wszystkich Swietych")
                || document.contains("Linia Specjalna - Kursuje tylko w dniu Wszystkich Swietych")) {
            document = convertNightToNormalSchedule(document);
        }
        //TODO druga proteza koniecpolska
        if (document.contains("SOBOTY,  ROBOCZE  W  LIPCU  I  SIERPNIU  ORAZ:  1.11,  24.12,  31.12")) {
            document = convertSatHolToNormalSchedule(document);
        }
        //TODO trzecia proteza koniecpolska - dla 69
        if (document.contains("<B>69 </B>")) {
            document = convert69ToNormalSchedule(document);
        }
        //podziel wg znacznika </TR>
        String[] parts = document.split("</TR>");
        //wyciągnij nagłówek...
        String[] header = scheduleHeader(parts[0]);
        //...i od razu go powrzucaj do rozkładu
        sch.setLine(header[0]);
        sch.setStop(header[1]);
        sch.setDirection(header[2]);
        //parts[1] - nagłówek tabeli, informacja o typach rozkładów (robocze, wakacyjne itp.)
        sch.setScheduleTypes(scheduleTypes(parts[1]));
        /*parts[2] - śmieci
         *w parts[3] i do końca - wyciąganie poszczególnych rozkładów
         * wiadomo, że parts[n-1] - śmieci htmlowe
         * a parts[n-2] - legenda...
         * 
         * UPDATE
         * wiadomo, że parts[n-2] - może legenda, może ostatni rozkład
         * parts[n-1] - śmieci
         */
        List < List <CourseStop> > tempList;
        for(int i=3;i<parts.length-1;++i) {
            if (!parts[i].contains("COLSPAN"))
            {
                tempList = parseOneLine(parts[i]);
                //powrzucaj wycięte godziny z danej linijki do zbiorników
                for(int j=0; j<sch.getScheduleTypes().length; ++j) {
                    //lista z rozkładów
                    List<CourseStop> mainStops = sch.getCourseStop(j);
                    mainStops.addAll(tempList.get(j));
                }
            }
        }
        /*legenda*/
        sch.setLegend(getLegend(parts[parts.length-2]));
        /*warto jeszcze posortować godzinki*/
        for(List<CourseStop> cstops : sch.getSchedules()) {
            java.util.Collections.sort(cstops);
        }
        
    }

    @Override
    public ParsedSchedule getParsedSchedule() {
        return sch;
    }
    
    /** Parsuje nagłówek z tabeli, wyciągając:
     * <ul>
     * <li>linię;</li><li>przystanek;</li><li>i kierunek</li>
     * </ul>
     * 
     * @param part
     * @return 
     */
    private String[] scheduleHeader(String part) {
        String[] result = new String[3];
        String temp = part.substring(part.indexOf("<TR>"),part.length()-1);
        //podziel po temp
        String[] temps = temp.split("<B>");
        //w temps[1] - numer linii
        String number = temps[1].substring(0,temps[1].indexOf(" </B>"));
        result[0] = number;
        //w temps[2] - przystanek
        result[1] = temps[2].substring(0,temps[2].indexOf("</B>"));
        //w temps[3] - kierunek
        result[2] = temps[3].substring(0,temps[3].indexOf("</B>"));
        return result;
        //zrobione :)
    }
    
    /** Funkcja parsująca "nagłówek" tabeli i zwracająca typy możliwych rozkładów (robocze itp.)
     * 
     * @param part
     * @return 
     */
    private schedules.ScheduleType[] scheduleTypes(String part) {
        ArrayList<schedules.ScheduleType> types = new ArrayList<schedules.ScheduleType>();
        //podzielenie wg "<TD COLSPAN=2 ALIGN="CENTER">"
        String[] temps = part.split("<TD COLSPAN=2 ALIGN=\"CENTER\">");
        //jedź po każdym i sprawdź jaki ma napis - a następnie wrzuć ewentualnie do wora
        //bardzo leniwie, nawet bez obrabiania
        //bardzo leniwie - proszę nie wrzucać na kursy programistyczne
        for(String str : temps) {
            if (str.equals("ROBOCZE</TD>") || str.equals("ROBOCZE OD WRZESNIA DO CZERWCA</TD>")) {
                types.add(schedules.ScheduleType.WORKING_DAYS);
            }
            if (str.equals("ROBOCZE  W  LIPCU  I  SIERPNIU</TD>")) {
                types.add(schedules.ScheduleType.SUMMER_DAYS);
            }
            if (str.equals("SOBOTY  ORAZ:   1.11,  24.12,  31.12</TD>")) {
                types.add(schedules.ScheduleType.SATURDAY_DAYS);
            }
            if (str.equals("NIEDZIELE  I  SWIETA</TD>")) {
                types.add(schedules.ScheduleType.HOLIDAYS);
            }
        }
        //maszyna wirtualna się buntuje - więc rzutowanie po chińsku
        schedules.ScheduleType[] result = new schedules.ScheduleType[types.size()];
        for(int i=0; i<types.size(); ++i) {
            result[i] = types.get(i);
        }
        return result;
    }
    
    /** parsuje jedną linię (ograniczoną znacznikami TR) i zwraca listę kolekcji godzin.
     * 
     * @return 
     */
    private List < List<CourseStop> > parseOneLine(String part) {
        /*Tyle list, ile rozkładów*/
        List <List<CourseStop>> schedules = new ArrayList<List<CourseStop>>(sch.getScheduleTypes().length);
        /*Podziel wg <TD ALIGN="CENTER">*/
        String[] temps = part.split("<TD ALIGN=\"CENTER\">");
        int tempHour;
        int tempMinute;
        /*Prócz temps[0], reszta ma postać taką:
         *       <B>4</B></TD>
                 <TD> 56a</TD>
                 * temps[1]-roboczy
                 * temps[2]-wakacyjne
                 * temps[3]-soboty
                 * temps[4]-niedziele
         */
        /*Zajmujemy się teraz po kolei każdym z rozkładów*/
        for(int i=1; i<sch.getScheduleTypes().length+1; ++i) {
            //utwórz tymczasową listę godzin
            List <CourseStop> tempList = new LinkedList<CourseStop>();
            //podziel wg </TD>
            String[] elements = temps[i].split("</TD>");
            /*Mamy tak
            *       <B>4</B>
            *       <TD> 56a
            */
            //z elements[0] trzeba wyciągnąć godzinkę - po chińsku powycinaj znaczniki
            String stupidHour = elements[0].replace("<B>", "").replace("</B>","");
            //sparsuj - i do tempHour
            tempHour = Integer.parseInt(stupidHour);
            //z elements[1] wytnij <TD>
            elements[1] = elements[1].replace("<TD>","");
            //jeżeli to, co zostało == "-", to nie ma nic do roboty
            if (elements[1].equals("-")) {
                //wypad
            } else {
                /*poucinaj wg spacji, powinno zostać np. "" i "56a"
                 */
                String[] singleMinutes = elements[1].split(" ");
                //jedź po wszystkich
                for(String singleMinute : singleMinutes) {
                    //jeżeli nie jest śmieciem
                    if (!singleMinute.equals("")) {
                        //wytnij same minuty
                        String minuteString = singleMinute.substring(0,2);
                        //wytnij same dodatkowe oznaczenia
                        String desc = singleMinute.substring(2);
                        //parsuj minuty
                        tempMinute = Integer.parseInt(minuteString);
                        //utwórz nowy obiekt CourseStop
                        tempList.add(new CourseStop((byte)tempHour,(byte)tempMinute,desc));
                    }
                }
            }
            //chyba wszystko
            //dorzuć do wyniku
            schedules.add(tempList);
        }
        
        return schedules;
        //działa :)
    }

    /**
     * 
     * @param part
     * @return 
     */
    private String getLegend(String part) {
        /* Zawodnik postaci
         *       <TR>
        <TD COLSPAN=8>a - kurs wydĹ‚uĹĽony do przystanku: WALCOWNIA<BR>b - 
        * kurs tylko do przystanku: PLAC DASZYĹSKIEGO(...)</TD>
         */
        //powycinaj znaczniki
        String temp = "Brak legendy w przypadku tej linii";
        if (part.contains("COLSPAN")) {
            temp = part.replaceAll("<TR>","").replaceAll("<TD COLSPAN=8>","").replaceAll("</TD>","");
            //zamień hasha na napis "Wakacyjne"
            temp = temp.replace("#","Wakacyjne");
            //zamien <BR> na znak nowej linii
            temp = temp.replaceAll("<BR>","\n");
        }
        //i tyle
        return temp;
    }

    @Override
    public String[] getUrlsOfLines() {
        return urlLines;
    }

    @Override
    public String[] getNameOfLines() {
        return lines;
    }
    
    @Override
    public String[] getCanonicalLines() {
        return canonicalLines;
    }

    @Override
    public void parseLineDocument(String doc, String baseDir) {
        //ilość wpisów bez sensu w liście katalogów + 1
        final int LAST_TRASH = 3;
        LinkedList<String> lines = new LinkedList<String>();
        LinkedList<String> canonicalLines = new LinkedList<String>();
        LinkedList<String> urls = new LinkedList<String>();
        //podziel po <ul>
        String[] temps = doc.split("<ul>");
        /*w pierwszej są śmieci, aczkolwiek zawiera baseDir
         * drugą część podziel po </li>
         * Powinno wyjść coś takiego:
         * <li><a href="/rozklady/"...
         * <li><a href="0001/"> 0001/</a></li>
         * ...
         * na razie trzy ostatnie - to śmiecie
         */
        String[] parts = temps[1].split("</li>");
        /*Pierwsza część nas wali, natomiast trzeba będzie pójść od pierwszej aż przed
         * trzy ostatnie.
         */
        for(int i=1; i<parts.length-LAST_TRASH;++i) {
            /*Jest tutaj coś takiego: 
             * <li><a href="0001/"> 0001/</a>
             */
            //w bardzo naiwny sposób wywal śmieci
            parts[i] = parts[i].replace("\n<li><a href=\"","").replace("/</a>","").replace("\">","");
            /*mamy coś takiego:
             * 0001/ 0001
             * Pierwszy jest urlem, drugi - linią, wycinając zera przed.
             * Ale najpierw - podziel po spacji
             */
            String[] elements = parts[i].split(" ");
            //wrzuć do canonical
            canonicalLines.add(elements[0].substring(0,elements[0].length()-1));
            //od razu zrób urla i wrzuć w listę
            urls.add(baseDir+elements[0]);
            /*W drugim elemencie trzeba pozbyć się nadmiarowych zer
             *Kolejne przycinania - aż będzie 0
             */
            while(elements[1].startsWith("0")) {
                elements[1] = elements[1].substring(1);
            }
            //wielkie litery
            elements[1] = elements[1].toUpperCase();
            //i wrzuć do listy
            lines.add(elements[1]);
        }
        //zamień na tablice stringów
        this.lines = lines.toArray(new String[0]);
        this.urlLines = urls.toArray(new String[0]);
        this.canonicalLines=canonicalLines.toArray(new String[0]);
    }
/* Część od parsowania tego wszystkiego.
 * Wiem, że to antywzorzec, ale niech zadziała, a potem będę się zastanawiam
 */
    private String line;
    private String[] directions;
    private List<OneDirectionParsedSchedules> pdirections = new LinkedList<OneDirectionParsedSchedules>();
    @Override
    public void parseDocuments(String line, String canLine, String url) {
        /*Masakra*/
        //od razu wpisz linię
        this.line = line;
        //ściągnij plik "canLineinfo", np. 0001info
        String infoDocument = loadURLToString(url+canLine+"info");
        //w tym pliku - podwójne entery oddzielają kierunki
        String[] directionParts = infoDocument.split("\n\n");
        //w pierwszej wyciep pierwszego entera
        directionParts[0] = directionParts[0].substring(1);
        //a w ostatniej - ostatniego entera
        //excellent
        directionParts[directionParts.length-1] = directionParts[directionParts.length-1]
                .substring(0,directionParts[directionParts.length-1].length()-1);
        //wiemy ile kierunków
        this.directions = new String[directionParts.length];
        //i ciachajmy party
        //i=0 lub i=1
        for(int i=0; i<this.directions.length; ++i) {
            OneDirectionParsedSchedules odps = parseOneDirectionPart(line, canLine, url, directionParts[i]);
            pdirections.add(odps);
            this.directions[i] = odps.getDirection();
        }
        //TODO
    }
    
    private OneDirectionParsedSchedules parseOneDirectionPart(String line, String canLine, String url, String part) {
        //utwórz rezultat
        OneDirectionParsedSchedules pschedules = new OneDirectionParsedSchedules("", line);
        //lista sparsowanych dokumentów
        List<ParsedSchedule> lpschedules = new LinkedList<ParsedSchedule>();
        //ciachnij znakiem nowego wiersza
        String[] parts = part.split("\n");
        /*Pierwszy part jest kierunkiem - i tak niepotrzebny, bo będziemy znać
         * z każdego z rozkładów.
         */
        StringBuffer sb = new StringBuffer();//kk
        for(int i=1; i<parts.length; ++i) {
            //podziel spacją
            String[] elements = parts[i].split(" ");
            //pierwszy jest plikiem (bez rozszerzenia), który zawiera rozkład
            String nurl = url+elements[0]+".htm";
             sb.append(elements[0]+" ");
            for(int j=1;j<elements.length;++j) {
                sb.append(elements[j]+" ");
            }
            sb.append('\n');//kk
            /*drugi jest nazwą przystanku, który i tak uzyskamy z przystanków
             * parsuj i do wora*/
            //przy okazji - zjedz samego siebie
            parseString(loadURLToString(nurl));
            lpschedules.add(sch);
        }
        //z pierwszego wydłub kierunek
        pschedules.setDirection(lpschedules.get(0).getDirection());
        System.out.println(line+" "+lpschedules.get(0).getDirection());
        System.out.println(sb.toString());
        //i wrzuć przystanki do rozkładów i wypad
        pschedules.setPschedules(lpschedules);
        //może jeszcze wypisz coś
        //System.out.println(line+" "+pschedules.getDirection());
        return pschedules;
    }

    @Override
    public String getLine() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getDirections() {
        return directions;
    }

    @Override
    public List<OneDirectionParsedSchedules> getParsedSchedules() {
        return pdirections;
    }
    
    /** Konwertuje pieprzony rozkład nocny na normalniejszy
     * 
     * @param document
     * @return 
     */
    private String convertNightToNormalSchedule(String document) {
        String temp = new String(document);
        LinkedList<String> partsToJoin = new LinkedList<String>();
        //podziel przez </TR>
        String[] parts = temp.split("</TR>");
        //pierwszy to nagłówek - nic do roboty
        partsToJoin.add(parts[0]+"</TR>");
        //drugi to utworzenie takiego śmiecia
        //jebać wydajność
        String stringWithTypes = "<TR BGCOLOR=\"#E0E0FF\">"
                + "<TD COLSPAN=2 ALIGN=\"CENTER\">ROBOCZE</TD>"
                + "<TD COLSPAN=2 ALIGN=\"CENTER\">ROBOCZE  W  LIPCU  I  SIERPNIU</TD>"
                + "<TD COLSPAN=2 ALIGN=\"CENTER\">SOBOTY  ORAZ:  1.11,  24.12,  31.12</TD>"
                + "<TD COLSPAN=2 ALIGN=\"CENTER\">NIEDZIELE  I  SWIETA</TD>"
                + "</TR>";
        partsToJoin.add(stringWithTypes);
        //parts[2] to śmieci i takie też damy
        partsToJoin.add("<TR>śmieci</TR>");
        //teraz jedziemy od parts[3] do momentu tuż przed legendą
        for(int i=3;i<parts.length-2;++i) {
            //wywal <TR>
            parts[i] = parts[i].replace("<TR>","");
            //utwórz tymczasowo taką linijkę: TR i 4*parts[i] i </TR>
            String tempPart = "<TR>"+parts[i]+parts[i]+parts[i]+parts[i]+"</TR>";
            //dodaj do partsów
            partsToJoin.add(tempPart);
        }
        //dodaj przedostatniego i ostatniego parta
        partsToJoin.add(parts[parts.length-2]+"</TR>");
        partsToJoin.add(parts[parts.length-1]);
        //robimy teraz wynikowego stringa
        StringBuffer sb = new StringBuffer("");
        for(int i=0; i<partsToJoin.size(); i++) {
            sb.append(partsToJoin.get(i));
        }
        return sb.toString();
    }
    
    public String convertSatHolToNormalSchedule(String doc) {
        String temp = new String(doc);
        LinkedList<String> partsToJoin = new LinkedList<String>();
        //podziel przez </TR>
        String[] parts = temp.split("</TR>");
        //pierwszy to nagłówek - nic do roboty
        partsToJoin.add(parts[0]+"</TR>");
        //drugi to utworzenie takiego śmiecia
        //jebać wydajność
        String stringWithTypes = "<TR BGCOLOR=\"#E0E0FF\">"
                + "<TD COLSPAN=2 ALIGN=\"CENTER\">ROBOCZE</TD>"
                + "<TD COLSPAN=2 ALIGN=\"CENTER\">ROBOCZE  W  LIPCU  I  SIERPNIU</TD>"
                + "<TD COLSPAN=2 ALIGN=\"CENTER\">SOBOTY  ORAZ:  1.11,  24.12,  31.12</TD>"
                + (parts[1].contains("NIEDZIELE  I  SWIETA")?"<TD COLSPAN=2 ALIGN=\"CENTER\">NIEDZIELE  I  SWIETA</TD>":"")
                + "</TR>";
        partsToJoin.add(stringWithTypes);
        //parts[2] to śmieci i takie też damy
        partsToJoin.add("<TR>śmieci</TR>");
        //teraz jedziemy od parts[3] do momentu tuż przed legendą
         for(int i=3;i<parts.length-1;++i) {
                if (!parts[i].contains("COLSPAN")) {
               //wywal <TR>
               parts[i] = parts[i].replace("<TR>","");
               //zróbmy tak - podziel po </TD>
               String[] tdParts = parts[i].split("</TD>");
               //utwórz taką linijkę - rozkład roboczy
               String tp = "<TR>"+tdParts[0]+"</TD>"+tdParts[1]+"</TD>";
               //powtórz wakacyjno/roboczy
               tp += tdParts[2]+"</TD>"+tdParts[3]+"</TD>"+tdParts[2]+"</TD>"+tdParts[3]+"</TD>";
               //jeżeli jest jeszcze niedzielny - to go też wrzuć
               if (tdParts.length>4) {
                   tp+=tdParts[4]+"</TD>"+tdParts[5]+"</TD>";
               }
               tp+="</TR>";
               //dodaj do partsów
               partsToJoin.add(tp);
            }
        }
        //dodaj przedostatniego i ostatniego parta
         if (parts[parts.length-2].contains("COLSPAN")) partsToJoin.add(parts[parts.length-2]+"</TR>");
        partsToJoin.add(parts[parts.length-1]);
        //robimy teraz wynikowego stringa
        StringBuffer sb = new StringBuffer("");
        for(int i=0; i<partsToJoin.size(); i++) {
            sb.append(partsToJoin.get(i));
        }
        return sb.toString();
    }
    
    public String getAllLinesFromStops(String fileName) {
        String url = MZD_PRZ+fileName;
        //tymczasowo - na 2 marca
        //url = "file:///E:/Projekty/NetBeansProjects/ckmkm/konwerter/mzd020313/przystan.htm";
        //splituj po łączach
        String[] parts = loadURLToString(url).split("<A HREF=\"");
        StringBuffer sb = new StringBuffer("");
        //kursuj po wszystkich kawałkach od 1 do przedostatniego
        for(int i=1; i<parts.length-1; i++) {
            //wyciągnij goły link do pliku
            String stopUrl = parts[i].substring(0,parts[i].indexOf("\">"));
            //mzd_prz+to i mamy gotowego linka
            String stopSource = loadURLToString(MZD_PRZ+stopUrl);
            //dla debuggingu - numerek linii i spacja
            System.out.print((i-1)+" ");
            //do sb dodaj wyciągnete dane o przystanku i entera
            sb = sb.append(getLinesFromStop(stopSource).toUpperCase()).append('\n');
            //przystanek się wyświetli, wciśnij entera dla debugingu
            System.out.print('\n');
        }
        return sb.toString();
    }
    
    public String getLinesFromStop(String stopSourceHTML) {
        //podziel dokument po "<A HREF=\""
        String[] parts = stopSourceHTML.split("<A HREF=\"");
        //w parts[0] ukryta jest nazwa przystanku
        String[] parts0el = parts[0].split("<B>");
        //jw - w parts0el[1]
        String stopName = parts0el[1].substring(0,parts0el[1].indexOf("</B>"));
        //będzie on w rezultacie
        StringBuffer sb = new StringBuffer(stopName);
        //rób wycieczkę po wszystkich linkach (częściach) prócz ostatniego i pierwszego
        for(int i=1;i<parts.length-1;i++) {
            //wytnij samego linka
            String link = parts[i].substring(0,parts[i].indexOf("\">"));
            //podziel wg "/"
            String[] elements = link.split("/");
            //ustaw sobie argumenty do metody getNDSNumber
            String longLineNumber = elements[1];
            //jeżeli w środku jest 90, 93,94,97 albo 98 - pierdol to
            if (!(longLineNumber.contains("90") || longLineNumber.contains("93") ||
                  longLineNumber.contains("94") || longLineNumber.contains("97") ||
                    longLineNumber.contains("99")
                    )) {
                String stopId = elements[2].substring(0,elements[2].indexOf(".htm"));
                String oneLineResult = getNDSNumber(longLineNumber, stopId);
                //dodaj średnik i ten oneLinecośtam
                sb = sb.append(';'+oneLineResult);
            }
        }
        //wypisz dla dupadebuggingu przystanek
        System.out.print(stopName);
        //wypieprz to, co masz wywalić
        return sb.toString();
    }
    
    /** Zwraca pojedynczy napis typu 24_0_2.
     * 
     * Oznacza on linia 24, kierunek 0, przystanek 2.
     * Taki "wskaźnik" na konkretny rozkład, potrzebny do ciułania konkretych linii
     * z przystanków - aby nie biegać po wszystkich liniach.
     * 
     * @param longLineNumber
     * @param stopid
     * @return 
     */
    public String getNDSNumber(String longLineNumber, String stopid) {
        String number = new String(longLineNumber);
        while(number.startsWith("0")) {
            number = number.substring(1);
        }
        //wyciułaj urla do pliku info danej linii
        String url = MZD_PRZ+longLineNumber+'/'+longLineNumber+"info";
        String source = loadURLToString(url);
        String sid = stopid.substring(5);
        //wyciułaj przystanki - po podwójnym enterze
        String[] fDirs = source.split("\n\n");
        //wypierdol z pierwszego kierunku pierwszy znak - nowego wiersza
        fDirs[0] = fDirs[0].substring(1);
        //idziemy po kierunkach
        for(int d=0; d<fDirs.length; d++) {
            String[] fStops = fDirs[d].split("\n");
            for(int s=1; s<fStops.length; s++) {
                //pojedynczy wiersz podziel tak: znaki od 5 do 8 (7) to msid
                String msid = fStops[s].substring(5,8);
                if (sid.equals(msid)) //jak tak - to koniec roboty. s-1, bo pierwszy wiersz to śmieci
                    return number+'_'+d+'_'+(s-1);
            }
        }
        return null;
    }

    private String convert69ToNormalSchedule(String document) {
        //jak ma soboty - nic nie rób
        if (document.contains("SOBOTY")) return document;
        String temp = new String(document);
        LinkedList<String> partsToJoin = new LinkedList<>();
        String[] parts = temp.split("</TR>");
        //nagłówek - nic nie zmieniać
        partsToJoin.add(parts[0]+"</TR>");
        //drugi to utworzenie takiego śmiecia
        //jebać wydajność
        String stringWithTypes = "<TR BGCOLOR=\"#E0E0FF\">"
                + "<TD COLSPAN=2 ALIGN=\"CENTER\">ROBOCZE</TD>"
                + "<TD COLSPAN=2 ALIGN=\"CENTER\">ROBOCZE  W  LIPCU  I  SIERPNIU</TD>"
                + "<TD COLSPAN=2 ALIGN=\"CENTER\">SOBOTY  ORAZ:  1.11,  24.12,  31.12</TD>"
                + (parts[1].contains("NIEDZIELE  I  SWIETA")?"<TD COLSPAN=2 ALIGN=\"CENTER\">NIEDZIELE  I  SWIETA</TD>":"")
                + "</TR>";
        partsToJoin.add(stringWithTypes);
        //parts[2] to śmieci i takie też damy
        partsToJoin.add("<TR>śmieci</TR>");
        for(int i=3;i<parts.length-1;++i) {
               if (!parts[i].contains("COLSPAN")) {
               //wywal <TR>
               parts[i] = parts[i].replace("<TR>","");
               //zróbmy tak - podziel po </TD>
               String[] tdParts = parts[i].split("</TD>");
               //utwórz taką linijkę - rozkład roboczy
               String tp = "<TR>"+tdParts[0]+"</TD>"+tdParts[1]+"</TD>";
               //powtórz wakacyjny i daj pustą sobotę
               tp += tdParts[2]+"</TD>"+tdParts[3]+"</TD>"+tdParts[2]+"</TD>"+"-"+"</TD>";
               //jeżeli jest jeszcze niedzielny - to go też wrzuć
               if (tdParts.length>4) {
                   tp+=tdParts[4]+"</TD>"+tdParts[5]+"</TD>";
               }
               tp+="</TR>";
               //dodaj do partsów
               partsToJoin.add(tp);
            }
        }
        //dodaj przedostatniego i ostatniego parta
         if (parts[parts.length-2].contains("COLSPAN")) partsToJoin.add(parts[parts.length-2]+"</TR>");
        partsToJoin.add(parts[parts.length-1]);
        //robimy teraz wynikowego stringa
        
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<partsToJoin.size(); ++i) {
            sb.append(partsToJoin.get(i));
        }
        return sb.toString();
        
        
        
        
    }
}
