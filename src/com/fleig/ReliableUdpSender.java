package com.fleig;

import sun.awt.Mutex;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

public class ReliableUdpSender {

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

            //seta timeout em 0,5 segundo
            socket.setSoTimeout(500);

            byte[] bufReceive = new byte[1024];
            while(true) {
                //cria um novo datagram
                DatagramPacket receivePacket = new DatagramPacket(bufReceive, bufReceive.length);
                try {
                    socket.receive(receivePacket);
                    verifyACK(receivePacket);
                } catch (SocketTimeoutException e){
                    //não recebeu resposta
                    System.out.println("ACK não recebido... reenviando pacote");
                    //reenviar pacote
                    socket.send(sendPacket);
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return new String(sendPacket.getData(), 0, sendPacket.getLength());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error sending UDP";
    }

    /*
    * Print ping data to the standard output stream.
    */
    private static void verifyACK(DatagramPacket request) throws Exception
    {
        // Obtain references to the packet's array of bytes.
        byte[] buf = request.getData();
        // Wrap the bytes in a byte array input stream,
        // so that you can read the data as a stream of bytes.
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        // Wrap the byte array output stream in an input stream reader,
        // so you can read the data as a stream of characters.
        InputStreamReader isr = new InputStreamReader(bais);
        // Wrap the input stream reader in a bufferred reader,
        // so you can read the character data a line at a time.
        // (A line is a sequence of chars terminated by any combination of \r and \n.)
        BufferedReader br = new BufferedReader(isr);
        // The message data is contained in a single line, so read this line.
        System.out.println("ACK recebido");

//        if(line.equals("ACK")) {
////            System.out.println("ACK");
//        } else {
//            System.out.println("não ACK");
//            throw new SocketTimeoutException();
//        }
    }
}

