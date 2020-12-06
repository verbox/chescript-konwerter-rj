/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
/**
 *
 * @author Piotr
 */
public class StringUtilities {
    public static String loadStreamToString(InputStream is) {
        StringBuilder result = new StringBuilder("");
              try {
     BufferedReader br = new BufferedReader(new InputStreamReader(is,"ISO-8859-2")); //nowy strumien
      

      

        String line;
        //proba odczytu
        while((line=br.readLine())!=null) {
                result = result.append(new String(line.getBytes(),"UTF-8")).append("\n");
                }
                br.close();
        }
      catch (UnsupportedEncodingException ex) {
          System.out.println("Błędne kodowanie");
      }
      catch (IOException ex) {
          System.out.println("Błąd z wczytaniem strumienia do stringa, "+ex.getMessage());
      }

     //TODO proteza koniecpolska
              
      String string = new String(result.toString()).replaceAll("Ó", "O").replaceAll("Ą","A")
              .replaceAll("Ż","Z").replaceAll("Ź","Z").replaceAll("Ś","S").replaceAll("Ę","E")
              .replaceAll("Ć","C").replaceAll("Ł","L").replaceAll("Ń","N").replaceAll("ó", "o").replaceAll("ą","a")
              .replaceAll("ż","z").replaceAll("ź","z").replaceAll("ś","s").replaceAll("ę","e")
              .replaceAll("ć","c").replaceAll("ł","l").replaceAll("ń","n").replaceAll("Ü","U");
              
      //String string = result.toString();
      return string;
    }
    
    public static String loadFileToString(String file) {
        try {
            return loadStreamToString(new FileInputStream(file));
        }
        catch (FileNotFoundException ex) {
            System.out.println("Nie znaleziono pliku");
            return "";
        }
    }
    //tutaj będą jaja
    public static String loadURLToString(String urlString) {
       try {
        URL url = new URL(urlString);
        return loadStreamToString(url.openStream());
       }
       catch (MalformedURLException ex) {
           System.out.println("Błąd z URLem, "+ex.getMessage());
           return "";
       }
       catch (IOException ex) {
           System.out.println("Błąd ze strumieniem w URLu, "+ex.getMessage());
           return "";
       }
    }
    
    public static String loadURLWithGetToString(String urlString) {
        try {
        URL url = new URL(urlString);
        URLConnection urlConn = url.openConnection();
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.34 Safari/534.24");
        urlConn.setRequestProperty("Accept", "text/html, text/plain");
        return loadStreamToString(urlConn.getInputStream());
       }
       catch (MalformedURLException ex) {
           System.out.println("Błąd z URLem, "+ex.getMessage());
           return "";
       }
       catch (IOException ex) {
           System.out.println("Błąd ze strumieniem w URLu, "+ex.getMessage());
           return "";
       }
    }
    
    public static void saveStringToFile(File file, String content) throws IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(file), "UTF-8"));
                try {
             out.write(content);
            } finally {
                out.close();
            }
    }
}
