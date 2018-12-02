package Agent;

import AuctionHouse.*;
import Bank.*;
import MessageHandling.Message;
import MessageHandling.MessageAnalyzer;
import MessageHandling.MessageTypes;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Bank class.
 * @author Danan High
 * @version 11-13-18
 */
public class TestBank implements Runnable {
    
    private ArrayList<Agent> agents; //list of agent accounts
    private ArrayList<String> auctionHouses; //list of auction house accounts
    private ArrayList<Account> accounts;
    private HashMap<Integer, Account> userAccounts;
    private int currentAccountNumber = 0;
    static private String address;
    static private int portNumber;
    
    /**
     It is static and at a known address (IP address and port number)
     It hosts
     a list of agent accounts
     a list of auction house accounts
     It shares the list of auction houses with agents having bank accounts
     It provides agents with secret keys for use in the bidding process
     It transfers funds from agent to auction accounts, under agent control
     It blocks and unblocks funds in agent accounts, at the request of action houses
     
     
     Will have a proxy
     
     Some sort of pending balance:
     every time you make a bid on a new item
     subtract that amount from pending balance
     
     
     
     we need to create the bank first
     */
    
    public static void main(String[] args) throws Exception {
        if (args.length >= 1) {
            portNumber = Integer.parseInt(args[0]);
        } else {
            System.out.println("Error: Invalid program arguments. The first argument must be the bank port number.");
            return;
        }
        
        TestBank bank = new TestBank(address, portNumber);
        
        (new Thread(bank)).start();
    }
    
    /**
     * Constructor for Bank
     *
     */
    public TestBank(String address, int portNumber){
        agents = new ArrayList<Agent>();
        auctionHouses = new ArrayList<>();
        accounts = new ArrayList<Account>();
        userAccounts = new HashMap<>();
    }
    
    @Override
    public void run(){
        
        try{
            ServerSocket server = new ServerSocket(portNumber);
            
            while (true) {
                Socket client = server.accept();
                ServerThread bank = new ServerThread(client, this);
                (new Thread(bank)).start();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Creates and returns an account.
     */
    public Account makeAccount(String name, double startingBalance) {
        Account account = new Account(name,
                                      assignAccountNumber(),
                                      startingBalance,
                                      startingBalance);
        
        if (!this.getAccounts().contains(account)) {
            this.getAccounts().add(account);
        }
        
        return account;
    }
    
    /**
     * Adds an auction house to the list of auction houses.
     */
    public void addAuctionHouse(AuctionHouse house){
//        this.auctionHouses.add(house);
    }
    
    /**
     * Assigns an account number to an agent and increments the current account number
     */
    private int assignAccountNumber() {
        int number = this.currentAccountNumber;
        this.currentAccountNumber++;
        return number;
    }
    
    /**
     * Gets the list of bank accounts
     * @return
     */
    public ArrayList<Account> getAccounts(){
        return accounts;
    }
    
    /**
     * Gets list of agents for a auction house.
     */
    public ArrayList<Agent> getAgents() {
        return agents;
    }
    
    /**
     * Gets list of auction houses for a agent.
     */
    public ArrayList<String> getAuctionHouses() {
        return auctionHouses;
    }
    
    
    /**
     * Transfers funds from an Agent account to an AuctionHouse account.
     */
    public synchronized void transferFunds(int auctionHouseAccountNumber,
                                           int agentAccountNumber,
                                           double amount) throws Exception {
        
        Account houseAccount = accounts.get(auctionHouseAccountNumber);
        Account agentAccount = accounts.get(agentAccountNumber);
        
        synchronized (houseAccount){
            synchronized (agentAccount){
                
                if(agentAccount.getPendingBalance() >= amount){
                    //transfer funds from agent to auction house
                    agentAccount.setPendingBalance(agentAccount.getPendingBalance() - amount);
                    agentAccount.setBalance(agentAccount.getBalance() - amount);
                    houseAccount.setBalance(houseAccount.getBalance() + amount);
                    houseAccount.setPendingBalance(houseAccount.getPendingBalance() + amount);
                }
                else{
                    throw new Exception(); //"Unable to transfer funds. The agent's pending balance is less than the specified amount."
                }
            }
        }
    }
    
    
    
    
    // private sub class
    private static class ServerThread implements Runnable {
    
        private final String NAME = "test bank";
    
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private TestBank bank;
        
        // constructor
        public ServerThread(Socket client, TestBank bank) {
            this.bank = bank;
            
            try {
                out = new ObjectOutputStream(client.getOutputStream());
                out.flush();
                in = new ObjectInputStream(client.getInputStream());
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        
        /**
         * Function to close the client from the server.
         */
        private void closeClient() {
            try {
                out.writeObject("Server has closed!");
                in.close();
                out.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }

        /**
         * Function to respond after message analysis
         */
        private Message response(Message message, MessageTypes type) {

            Message response = null;
            ArrayList<Object> list = message.getMessageList();
            
            switch (type) {
                case THANKS:
                    break;
                case GET_HOUSES:
                    System.out.println(message);
                    response = new Message(NAME,
                                           MessageTypes.HOUSES,
                                           bank.getAuctionHouses());
                    break;
                case ACCOUNT_INFO:
                    response = new Message(NAME,
                                           MessageTypes.ACCOUNT_INFO,
                                           new Account("test",
                                                       1,
                                                       98,
                                                       98));
                    break;
                case GET_AGENT_ID_FOR_HOUSE:
                    response = new Message(NAME,
                                           MessageTypes.GET_AGENT_ID_FOR_HOUSE,
                                           9);
                    break;
                case REMOVE_FUNDS:
                    int accountID = (int) list.get(2);
                    double withdraw = (double) list.get(3);
                    
                    for (Integer i: bank.userAccounts.keySet()) {
                        if (accountID == i) {
                            Account account = bank.userAccounts.get(i);
                            account.setBalance(account.getBalance() - withdraw);
                        }
                    }
                    response = new Message(NAME,
                                           MessageTypes.CONFIRMATION);
                    break;
                case CREATE_ACCOUNT:
                    System.out.println(message);
                    Account account = (Account) list.get(2);
                    if (!bank.userAccounts.containsKey(account.getAccountNumber())) {
                        bank.userAccounts.put(account.getAccountNumber(), account);
                        response = new Message(NAME,
                                               MessageTypes.CONFIRMATION);
                    } else {
                        response = new Message(NAME,
                                               MessageTypes.ACCOUNT_EXISTS);
                    }
                    
                    break;
            }
            return response;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            MessageAnalyzer analyzer = new MessageAnalyzer();
            boolean connected = true;
            Message message = null;

            try {
                do {
                    try {
                        // get message from the sender, analyze and respond
                        message = (Message) in.readObject();
                        if (message != null) {
                            int analysis = analyzer.analyze(message);
                            out.writeObject(response(message,
                                                     (MessageTypes) message.getMessageList()
                                                                           .get(1)));
                        }

                    } catch (ClassNotFoundException cnf) {
                        cnf.printStackTrace();
                    } catch (EOFException eof) {
                        connected = false;
                        System.out.println("Client has disconnected!");
                        break;
                    }
                } while (connected);
                closeClient();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }
    
    
    
    
    
    //TODO
    
    
    /**
     * Handles messages received from Houses and Agents.
     */
}
