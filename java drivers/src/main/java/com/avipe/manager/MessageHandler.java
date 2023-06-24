package com.avipe.manager;

import com.avipe.control.Main;

public class MessageHandler {
    public static void actions(String msg){
        if(msg != null){
            try{
                Main.request.sendPost(msg);
            }catch (Exception e) {
                System.out.println("Error to Send Data To api");
            }
        }
    }
}
