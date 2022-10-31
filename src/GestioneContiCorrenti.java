import com.google.gson.stream.*;
import com.google.gson.Gson;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

public class GestioneContiCorrenti {

    public static Map<String, LongAdder> m = new ConcurrentHashMap<>();

    public static class Transazione{
        private String date;
        private String reason;

        public Transazione (String date, String reason){
            this.date = date;
            this.reason = reason;
        }

        public String getDate() {
            return date;
        }

        public String getReason() {
            return reason;
        }

    }

    public static class ContoCorrente implements Runnable {
        private String owner;
        private Transazione[] records;

        public ContoCorrente (String owner, Transazione[] records) {
            this.owner = owner;
            this.records = records;
        }

        public String getOwner() {
            return owner;
        }

        public Transazione[] getRecords() {
            return records;
        }

        public void run() {
            for (Transazione t: records)
                m.computeIfAbsent((t.getReason()).toUpperCase(), k -> new LongAdder()).increment();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        JsonReader reader;

        Gson gson = new Gson();

        ExecutorService service = Executors.newFixedThreadPool(20);

        try {
            reader = new JsonReader(new FileReader("C:\\Users\\cozzo\\OneDrive\\Desktop\\accounts.json"));
            reader.beginArray();
            while (reader.hasNext()) {
                ContoCorrente contoCorrente = gson.fromJson(reader, ContoCorrente.class);
                service.execute(contoCorrente);
            }
            reader.endArray();
            reader.close();
        } catch (FileNotFoundException ex) {
            System.err.print(ex.getMessage());
        } catch (IOException e) {
            System.err.print(e.getMessage());
        }

        service.shutdown();

        if (!service.awaitTermination(60000, TimeUnit.SECONDS))
            System.err.println("I thread non sono stati completati entro i tempi!");

        Iterator<String> it = m.keySet().iterator();

        while (it.hasNext()) {
            String s = (String) it.next();
            System.out.println(s + ": " + m.get(s));
        }

        System.exit(0);

    }

}