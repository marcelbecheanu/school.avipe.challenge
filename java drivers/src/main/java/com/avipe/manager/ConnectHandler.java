package com.avipe.manager;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;

public class ConnectHandler {
    private String msg = "";
    public void connect(String portname) {
        SerialPort port = new SerialPort(portname);
        try {
            port.openPort();

            port.setParams(
                    SerialPort.BAUDRATE_57600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );

            port.addEventListener((SerialPortEvent event) -> {
                if(event.isRXCHAR()) {
                    try {
                        String s = port.readString();
                        if(s != null){
                            msg += s;
                            msg.replace("\n", "");
                            if(msg.length() > 1 && !s.startsWith("|") && msg.contains("|") ){
                                System.out.print(msg);
                                MessageHandler.actions(msg);
                                msg = "";
                            }
                        }
                    } catch (SerialPortException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SerialPortException e) {
            e.printStackTrace();
        }

    }
}
