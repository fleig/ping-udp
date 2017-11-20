package com.fleig;

import sun.awt.Mutex;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    static DatagramSocket socket;
    static InetAddress address;

    static int port = 4445;

    static long minRtt = Integer.MAX_VALUE;
    static long maxRtt = 0;
    static long totRtt = 0;
    static int index;

    static Mutex mutex = new Mutex();

    static long incIndex(){
        long aux;

        mutex.lock();
        aux = index;
        index++;
        mutex.unlock();

        return aux;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("------START-----");

        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");

        long numPing = 10;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                long i = incIndex();

                if(i>numPing-1) {
                    this.cancel();
                    System.exit(0);
                }

                LocalTime time = LocalTime.now();
//                System.out.print("ini>> " + time);

                String send = sendUDP("PING " + i + " " + time + "\r\n");
//                System.out.print(send);

                LocalTime timeAft = LocalTime.now();
                long rtt = time.until(timeAft, ChronoUnit.MILLIS);

                System.out.println(send.replace("\r\n","") + "\t" + rtt + "ms");

                totRtt += rtt;
                if(rtt < minRtt)
                    minRtt = rtt;
                if(rtt > maxRtt)
                    maxRtt = rtt;
            }

            @Override
            public boolean cancel() {
                System.out.println();
                System.out.println("---- Statistics ----");
                System.out.println("minRtt: " + minRtt + " maxRtt: " + maxRtt + " avgRtt: " + totRtt/numPing);

                System.out.println("------END-----");
                socket.close();

                return super.cancel();
            }
        };

        Timer timer = new Timer();

        //agenda task para executar a cada 1 segundo
        timer.scheduleAtFixedRate(task, 0, 1000);
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
