package ru.volxard;

import com.google.gson.Gson;
import lombok.*;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final OkHttpClient client;
    private final Gson gson;
    private final Semaphore semaphore;

    public CrptApi(int requestLimit, long time, TimeUnit timeUnit) {
        client = new OkHttpClient();
        gson = new Gson();
        semaphore = new Semaphore(requestLimit);

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(timeUnit.toMillis(time));
                    int permitsToRelease = requestLimit - semaphore.availablePermits();
                    semaphore.release(permitsToRelease);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void createDocument(Document document, String signature) {
        try {
            semaphore.acquire();
            String json = gson.toJson(document);
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();
            client.newCall(request).execute();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CrptApi api = new CrptApi(10, 1, TimeUnit.MINUTES);
        Document document = new Document();
        api.createDocument(document, "signature");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;
        private String reg_date;
        private String reg_number;



        @Getter
        @Setter
        @AllArgsConstructor
        public static class Description {
            private String participantInn;
        }



        @Getter
        @Setter
        @AllArgsConstructor
        public static class Product {
            private String certificate_document;
            private String certificate_document_date;
            private String certificate_document_number;
            private String owner_inn;
            private String producer_inn;
            private String production_date;
            private String tnved_code;
            private String uit_code;
            private String uitu_code;


        }
    }
}