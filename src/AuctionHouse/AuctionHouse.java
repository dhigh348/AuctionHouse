package AuctionHouse;

import Agent.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class AuctionHouse implements Runnable{
    private int bidderTally;
    private String type;
    private List<Item> itemList;
    private List<BidCoord> auctions;
    private MakeItems makeItems;
    private ServerSocket serverSocket;
    private int port;
    private String serverName;

    /**
     * Expects an int that represents what type
     * auction house will be. 1 for furniture, 2 for tech
     * and any other number for car.
     * @param type An int
     */
    public AuctionHouse(String type,String port,String serverName) {
        try {
            bidderTally = 0;
            makeItems = new MakeItems();
            itemList = makeItems.getItems(Integer.parseInt(type));
            this.type = makeItems.getListType();
            this.port=Integer.parseInt(port);
            this.serverName=serverName;
            serverSocket = new ServerSocket(this.port);
        }catch(IOException i){
            System.out.println(i);
        }
    }

    /**
     * Used to get port number that server is on.
     * @return An int, is the port number.
     */
    public int getPort(){
        return port;
    }

    /**
     * Used to get the name of computer that server is running
     * on.
     * @return Name of server,String
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Returns the items for sale in auction house.
     * @return a list.
     */
    public List<Item> getItemList(){
        return itemList;
    }

    /**
     * Finds the most expensive item
     * in auction is and gets the price.
     * @return An int that is max price
     */
    public int maxPrice(){
        int max=0;
        for(Item t:itemList){
            if(t.getPrice()>max)max=t.getPrice();
        }
        return max;
    }

    /**
     * Gets the type of items sold at an
     * auction.
     * @return A string that is type.
     */
    public String getType() {
        return type;
    }

    /**
     * Finds the cheapest item in auction
     * and gets its price.
     * @return
     */
    public int lowestPrice(){
        int min=1000;
        for(Item t:itemList){
            if(t.getPrice()<min)min=t.getPrice();
        }
        return min;
    }

    @Override
    public void run(){
        while(true){
            try {
                System.out.println("waiting for agents");
                Socket agent = serverSocket.accept();
                Thread t=new Thread(new Agent());
                t.start();
            }catch(IOException i){
                System.out.println(i);
            }
        }
    }

    public static void main(String[] args){
        AuctionHouse auctionHouse=new AuctionHouse(args[0],args[1],args[3]);
        Thread t=new Thread(auctionHouse);
        t.start();
    }
}
