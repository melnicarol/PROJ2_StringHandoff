package com.abc.handoff;

import com.abc.pp.stringhandoff.*;
import com.programix.thread.*;

//This program has multiple threads passing a string to other threads

public class StringHandoffImpl implements StringHandoff {
//String to hold the message to be passed
	private String message;
	 
    public StringHandoffImpl() {
//set the message to null to use it as a flag
    	 message = null;
    }
//passing string calls this function
    @Override
    public synchronized void pass(String msg, long msTimeout)
            throws InterruptedException,
                   TimedOutException,
                   ShutdownException,
                   IllegalStateException {
    	
//if message is null change the message
        if (message == null) {
            message = msg;
            notifyAll();
        }
//wqait if message isn't null
        if (msTimeout == 0L) {
            while (message != null) {
                wait();
            }
            message = msg;
            notifyAll();
        }

        long endTime = System.currentTimeMillis() + msTimeout;
        long msRemaining = msTimeout;
//Wait for a certain time, correcting for accuracy
        while (message != null && msRemaining > 0L) {
            wait(msRemaining);
            msRemaining = endTime - System.currentTimeMillis();
        }
        if (message == null) {
            message = msg;
            notifyAll();
        } else {
            throw new TimedOutException();
        }
    }
//same as previous method with no timeout limitation
    @Override
    public synchronized void pass(String msg)
            throws InterruptedException,
                   ShutdownException,
                   IllegalStateException {

    	   while (message != null) {
               wait();
           }
           message = msg;
           notifyAll();
    }
//Receiving thread's function
    @Override
    public synchronized String receive(long msTimeout)
            throws InterruptedException,
                   TimedOutException,
                   ShutdownException,
                   IllegalStateException {
//variable to hold passing message
    	 String receivedMessage;
         if (message != null) {
//If message is not null recieve message change flag notify threads        	 
             receivedMessage = message;
             message = null;
             notifyAll();
             return receivedMessage;
         }

         if (msTimeout == 0L) {
             while (message == null) {
                 wait();
             }
             receivedMessage = message;
             message = null;
             notifyAll();
             return receivedMessage;
         }
//wait if message isn't available correct for time
         long endTime = System.currentTimeMillis() + msTimeout;
         long msRemaining = msTimeout;

         while (message == null && msRemaining > 0L) {
             wait(msRemaining);
             msRemaining = endTime - System.currentTimeMillis();
         }
         if (message != null) {
             receivedMessage = message;
             message = null;
             notifyAll();
             return receivedMessage;
         }
         throw new TimedOutException();
    }
//same method as above without the timeout limitation
    @Override
    public synchronized String receive()
            throws InterruptedException,
                   ShutdownException,
                   IllegalStateException {

    	   while (message == null) {
               wait();
           }
           String receivedMessage = message;
           message = null;
           notifyAll();
           return receivedMessage;
    }

    @Override
    public synchronized void shutdown() {
        throw new RuntimeException("not implemented yet"); // FIXME
    }

    @Override
    public Object getLockObject() {
        return this;
    }
}