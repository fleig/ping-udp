package com.fleig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Main {

    static DatagramSocket socket;
    static InetAddress address;

    static int port = 4445;

    public static void main(String[] args) throws Exception {
        System.out.println("------START-----");

        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        String msg = "";

        long minRtt = Integer.MAX_VALUE;
        long maxRtt = 0;
        long totRtt = 0;

        long numPing = 10;


        for(int i =0; i<numPing; i++) {
            LocalTime time = LocalTime.now();

            String send = sendUDP("PING " + i + " " + time + "\r\n");
            System.out.print(send);

            LocalTime timeAft = LocalTime.now();
            long rtt = time.until(timeAft, ChronoUnit.MILLIS);

            totRtt += rtt;
            if(rtt < minRtt)
                minRtt = rtt;
            if(rtt > maxRtt)
                maxRtt = rtt;
        }

        System.out.println("minRtt: " + minRtt + " maxRtt: " + maxRtt + " avgRtt: " + totRtt/numPing);

        System.out.println("------END-----");
        socket.close();
    }

    static String sendUDP(String msg){
        try {
            //põe a mensagem no buffer
            byte[] bufSend = msg.getBytes();
            //cria um datagram a partir do buffer
            DatagramPacket sendPacket = new DatagramPacket(bufSend, bufSend.length, address, port);
            //envia o datagram
            socket.send(sendPacket);

            //seta timeout em 1 segundo
            socket.setSoTimeout(1000);

            byte[] bufReceive = new byte[1024];
            while(true) {
                //cria um novo datagram
                DatagramPacket receivePacket = new DatagramPacket(bufReceive, bufReceive.length);
                try {
                    //recebe o datagram de resposta
                    socket.receive(receivePacket);
                } catch (SocketTimeoutException e){
                    //não recebeu resposta
                    //reenviar pacote
                    socket.send(sendPacket);
                    continue;
                }

                return new String(sendPacket.getData(), 0, sendPacket.getLength());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error sending UDP";
    }
}
