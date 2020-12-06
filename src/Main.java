/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import output.generator.SCDOutputGenerator;
import parsing.*;
import static utilities.StringUtilities.*;
/**
 *
 * @author Piotr
 */
public class Main {
    public static void main(String[] argv) {
        testParseRecznie();
        //testGetInJava();
        //testStopLines();
    }
    
    public static void test1() {
        //zbuduj parsera
        ScheduleParser parser = new ScheduleParser(new HTMLBruteForceParseEngine());
        //wyciągnij plik testowy
        String doc = loadURLToString("http://www.mzd.czest.pl/rozklady/rozklady/0001/0001t005.htm");
        ParsedSchedule sch = parser.parseDocument(doc);
        System.out.println(sch);
    }
    
    public static void testURL() {
        //samo wczytanie urla
        System.out.println(loadURLToString("http://www.mzd.czest.pl/rozklady/rozklady/0028/0028t001.htm"));
    }
    
    public static void testMakeS() {
        //zbuduj parsera
        ScheduleParser parser = new ScheduleParser(new HTMLBruteForceParseEngine());
        //wyciągnij plik testowy
        String doc = loadURLToString("http://www.mzd.czest.pl/rozklady/rozklady/0001/0001t005.htm");
        ParsedSchedule sch = parser.parseDocument(doc);
        SCDOutputGenerator scdg = new SCDOutputGenerator();
        //scdg.generateOneSchedule("1\\", "0", "0", sch);
        System.out.println("OK");
    }
    
    public static void testLinesParser() {
        String baseUrl = "http://www.mzd.czest.pl/rozklady/rozklady_18_06_2012/";
        String doc = loadURLToString(baseUrl);
        LineParser parser = new LineParser(new HTMLBruteForceParseEngine(),doc,baseUrl);
        SCDOutputGenerator scdg = new SCDOutputGenerator();
        scdg.generateListOfLines("roz.scd", parser.getLines());
    }
    
    public static void testParseFullLine() {
        String url = "http://mzd.czest.pl/rozklady/rozklady_18_06_2012/0001/";
        String line = "1";
        String canLine = "0001";
        new HTMLBruteForceParseEngine().parseDocuments(line, canLine, url);
    }
    
    public static void testParseWithThisStupidOutput() {
        String baseUrl = "http://www.mzd.czest.pl/rozklady/rozklady_18_06_2012/";
        String doc = loadURLToString(baseUrl);
        LineParser parser = new LineParser(new HTMLBruteForceParseEngine(),doc,baseUrl);
        for(int i=0; i<parser.getLines().length; ++i) {
            new HTMLBruteForceParseEngine().parseDocuments(parser.getLines()[i]
                    , parser.getCanonicalLines()[i], parser.getUrlLines()[i]);
        }
    }
    
    public static void testParseRecznie() {
        String baseUrl = "http://mzd.czest.pl/images/rozklady/rozklady/";
        String doc = loadURLToString(baseUrl);
        LineParser parser = new LineParser(new HTMLBruteForceParseEngine(),doc,baseUrl);
        SCDOutputGenerator scdg = new SCDOutputGenerator();
        scdg.generateListOfLines("lines.scd", parser.getLines());
        HTMLBruteForceParseEngine eng;
        for (int i=1; i<parser.getLines().length; ++i)
        {
            //if (parser.getLines()[i].contains("69")) {//tymczasowo
            eng = new HTMLBruteForceParseEngine();
            eng.parseDocuments(parser.getLines()[i]
                    , parser.getCanonicalLines()[i], parser.getUrlLines()[i]);
            String line = parser.getLines()[i];
            scdg.generateOneLine(line, line, eng.getParsedSchedules());
            //}
            //lista kierunków z rozkładami
            /*
            for(int j=0; j<eng.getParsedSchedules().size(); j++) {
                scdg.generateFilesOneDirection(line, line, Integer.toString(j)
                        , eng.getParsedSchedules().get(j));
            }
            */
        }
        /* ale gówno - nie czytać!
         *         eng.parseDocuments(parser.getLines()[0]
                    , parser.getCanonicalLines()[0], parser.getUrlLines()[0]);
        scdg.generateFilesOneDirection("1","1","0",eng.getParsedSchedules().get(0));
        scdg.generateFilesOneDirection("1","1","1",eng.getParsedSchedules().get(1));
        scdg.generateFilesOneDirection("1","1","2",eng.getParsedSchedules().get(2));
        scdg.generateFilesOneDirection("1","1","3",eng.getParsedSchedules().get(3));
         */
    }
    
    public static void testGetInJava() {
        //wyciągnij "główną" stronę pksu z listą przystanków:
        String pksUrl = "http://www.pks-czestochowa.pl/index.php?kierunek=tam&lg=pl&mobile=";
        String docPks = loadURLWithGetToString(pksUrl);
        //pozamieniaj co trzeba
        
        //podziel po href"
        String[] parts = docPks.split("href=\"");
        List<String> urls = new LinkedList<String>();
        //idz od pierwszego do końca
        for(int i=1; i<parts.length; i++) {
            //wytnij śmieci
            String temp = parts[i].substring(0,parts[i].indexOf('\"'));
            //jak zawiera "kierunek=tam", to jest okej
            if (temp.contains("kierunek=tam")) {
                temp = temp.replaceAll("ó", "%F3").replaceAll(" ", "%20").
                        replaceAll("ę","%EA").replaceAll("ą","%B1").replaceAll("Ł","%A3").replaceAll("ł","%B3").
                        replaceAll("ś","%B6").replaceAll("Ś","%A6").replaceAll("ż","%BF").replaceAll("ń","%F1").
                        replaceAll("ź","%BC");
                urls.add(temp);
            }
        }
        //wypisz rozmiar
        System.out.println(urls.size());
        int j = 0;
        //jeźdź po wszystkich
        for(String url : urls) {
            j++;
            //wyciągnij nazwę przystanku
            String elements = url.split("rozklad=")[1];
            elements = elements.substring(0,elements.indexOf('&'));
            //przygotuj adres
            String usedUrl = "http://www.pks-czestochowa.pl/"+url;
            //wyciągnij zawartość
            String source = loadURLWithGetToString(usedUrl);
            //jak source!=docPks - sukces
            if (!source.equals(docPks)) j++;
            //zmień kodowanie
            source = source.replace("iso-8859-2","utf-8");
            //i zapisz
            try {
                saveStringToFile(new File("pks/"+elements+".html"), source);
            }
            catch (Exception ex) {}
            
        }
        System.out.println(j);
    }
    
    public static void testStopLines() {
        HTMLBruteForceParseEngine parseEngine = new HTMLBruteForceParseEngine();
        String result = parseEngine.getAllLinesFromStops("przystan.htm");
        try {
            saveStringToFile(new File("stops.scd"), result);
        } catch (Exception ex) {
            
        }
        
    }
}
