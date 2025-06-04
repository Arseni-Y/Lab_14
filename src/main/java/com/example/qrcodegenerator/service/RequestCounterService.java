package com.example.qrcodegenerator.service;

public class RequestCounterService {
    private static RequestCounterService instance;
    private long requestCount;

    private RequestCounterService() {
        this.requestCount = 0;
    }

    public static synchronized RequestCounterService getInstance() {
        if (instance == null) {
            instance = new RequestCounterService();
        }
        return instance;
    }

    public synchronized void incrementCount() {
        requestCount++;
    }

    public synchronized long getRequestCount() {
        incrementCount();
        return requestCount;
    }

    public synchronized void reset() {
        incrementCount();
        requestCount = 0;
    }
}