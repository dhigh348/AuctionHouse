package Proxies;

import Agent.*;
import AuctionHouse.*;
import Bank.Bank;
import MessageHandling.Message;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Proxies.AuctionHouseProxy is the proxy for the auction house class. The proxy
 * provides high level functionality as a mediary between the actual auction
 * house and the agent.
 * Danan High, 11/15/2018
 */
public class AuctionHouseProxy implements Runnable {

    private Bank bank;
    private Agent agent;
    private String host;
    private int port;
    private boolean connected = true;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket client = null;
    private AuctionHouse house;
    private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    
    /**
     * Constructor for the bank proxy.
     * Builds a reference to the bank for bank functionality
     */
    public AuctionHouseProxy(String host,
                             int port,
                             Agent agent,
                             Bank bank) {
        this.host = host;
        this.port = port;
        this.agent = agent;
        this.bank = bank;
        connectToServer();
    }
    
    /**
     * Setting up the input and output streams for the client connection.
     */
    private void setupInputAndOutputStreams() {
        try {
            if (client != null) {
                out = new ObjectOutputStream(client.getOutputStream());
                out.flush();
                in = new ObjectInputStream(client.getInputStream());
            } else {
                System.out.println("Client has not been connected");
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }
    
    /**
     * Agent connecting to the bank through the proxy.
     */
    private void connectToServer() {
        try {
            client = new Socket(host, port);
            setupInputAndOutputStreams();
            (new Thread(this)).start();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
    
    /**
     * Adding a message to the banks input stream.
     */
    @SuppressWarnings("unchecked")
    public void sendMessage(Message inMessage) {
        try {
            messageQueue.put(inMessage);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    
    /**
     * Overriding the run method to perform specialized tasks.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try {
            Message response = null, messageInput = null;
            
            do {
                try {
                    messageInput = messageQueue.take();
                    if (messageInput != null) {
                        out.writeObject(messageInput);
                    }
                    
                    // testing code to read from the server
                    response = (Message) in.readObject();
                    if (agent != null) {
                        if (response != null) {
                            agent.addMessage(response);
                        }
                    } else if (house != null) {
//                            bank.addMessage(response);
                    }
                } catch (EOFException eof) {
                    agent.setConnected();
                    out.close();
                    in.close();
                    System.out.println("Server has been closed");
                    break;
                } catch (ClassNotFoundException cnf) {
                    cnf.printStackTrace();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            } while (connected);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}





































