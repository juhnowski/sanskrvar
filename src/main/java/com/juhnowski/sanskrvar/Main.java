/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.juhnowski.sanskrvar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author ilya
 */
public class Main {
    SortedSet<String> sl;
    SortedSet<String> searchList;
    
    SortedSet<Entry> ts;
    public SortedSet<Entry> entrySet;
    ObjectMapper mainMapper = new ObjectMapper();
    
    public Main() {
        
        this.sl = new TreeSet<>();
        searchList = Collections.synchronizedSortedSet(new TreeSet(sl));
        
        this.ts = new TreeSet<>();
        entrySet = Collections.synchronizedSortedSet(new TreeSet(ts));
    }
    
    public class Entry implements Comparable{

        public String word;
        public String html;
        

        public Entry(String word, String html) {
            
            this.word = word;
            this.html = html;
        }

        @Override
        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            String s = "";
            try {
                s = mapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return s;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }

            Entry e = (Entry) o;

            return this.word.equals(e.word);
        }

        @Override
        public int hashCode() {
            return word.hashCode();
        }

        @Override
        public int compareTo(Object t) {
            if (!(t instanceof Entry)) {
                return 0;
            }

            Entry e = (Entry) t;
            return this.word.compareTo(e.word);
        }
    }

    public static void main(String[] args) {
        String word = "Siva";
        Main m = new Main();
        Entry e = m.checkWord(word);
        if (e != null) {
            m.entrySet.add(e);
            try {
            System.out.println(m.mainMapper.writeValueAsString(m.entrySet));
            }catch(JsonProcessingException ex){
                ex.printStackTrace();
            }
        }
    }

    public Entry checkWord(String word) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://www.sanskrit-lexicon.uni-koeln.de/scans/WILScan/2014/web/webtc/getword.php?key=");
        sb.append(word);
        sb.append("&filter=roman&noLit=off&transLit=hk");
        String targetURL = sb.toString();
        String urlParameters = "";

        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/xhr");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();
 
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); 
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String s = response.toString();
            if (!s.contains("<h2>not found:")) {
                return new Entry(word, s);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
    
    public SortedSet<Entry> getVariants(String word){
        doVariant("",word);
        return entrySet;
    }
}
