package com.avipe.control;
import com.avipe.manager.ConnectHandler;
import com.avipe.manager.RequestHandler;
import jssc.SerialPortList;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static String api = "http://94.63.81.0:9000/place/data";
    private static ConnectHandler connector = new ConnectHandler();
    public static RequestHandler request = new RequestHandler();

    public static void main(String[] args) {
        String portlist[] = SerialPortList.getPortNames();
        int value = 0;
        do{
            try{
                System.out.print("\033[H\033[2J");
                System.out.flush();
                for(int i =0; i<portlist.length;i++) {
                    System.out.println(">> "+i+" -  " + portlist[i]);
                }
                System.out.println("\n>> Select: ");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(System.in));
                value = Integer.parseInt(reader.readLine());

                if(value >= 0 && value <= portlist.length){
                    break;
                }else{
                    System.out.println("Invalid Value");
                }
            }catch (Exception ex){
                System.out.println("Invalid Value");
            }
        }while (true);
        connector.connect(portlist[value]);
    }

}
