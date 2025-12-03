package org.example.service;

public interface RateLimiter {

    /*
    * Returns true if the user's actions are allowed, under the current limits, false if throttled.
    * */
    public boolean allow(String userId);

//    public int capacity(String userId);
//
//    public int refillRate(String userId);



}
