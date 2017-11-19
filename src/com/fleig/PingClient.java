package com.fleig;

import java.io.IOException;
import java.net.*;

public class PingClient {
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] bufSend;
    private byte[] bufReceive = new byte[1024];

    private int port = 4445;

    public PingClient() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("localhost");
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

    }

    String sendUDP(String msg){
        try {
            //põe a mensagem no buffer
            bufSend = msg.getBytes();
            //cria um datagram a partir do buffer
            DatagramPacket sendPacket = new DatagramPacket(bufSend, bufSend.length, address, port);
            //envia o datagram
            socket.send(sendPacket);

            //seta timeout em 1 segundo
            socket.setSoTimeout(6000);

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

    public void close(){
        socket.close();
    }
}
