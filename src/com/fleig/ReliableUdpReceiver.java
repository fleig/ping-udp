package com.fleig;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class ReliableUdpReceiver {
    private static final double LOSS_RATE = 0.5;
    private static final int AVERAGE_DELAY = 300; // milliseconds

    public static void main(String[] args) throws Exception
    {
        System.out.println("Server started");
        // Get command line argument.
        if (args.length != 1) {
            System.out.println("Required arguments: port");
            return;
        }
        int port = Integer.parseInt(args[0]);
        // Create random number generator for use in simulating
        // packet loss and network delay.
        Random random = new Random();
        // Create a datagram socket for receiving and sending UDP packets
        // through the port specified on the command line.
        DatagramSocket socket = new DatagramSocket(port);
        // Processing loop.
        while (true) {
            // Create a datagram packet to hold incomming UDP packet.
            DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
            // Block until the host receives a UDP packet.
            socket.receive(request);

            // Print the recieved data.
            printData(request);
            // Decide whether to reply, or simulate packet loss.
            if (random.nextDouble() < LOSS_RATE) {
                System.out.println("ACK not sent.");
                continue;
            }
            // Simulate network delay.
            Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));

            InetAddress clientHost = request.getAddress();
            int clientPort = request.getPort();

            String sAck = "ACK\r\n";
            byte[] bufAck = sAck.getBytes();
            DatagramPacket packetAck = new DatagramPacket(bufAck, bufAck.length, clientHost, clientPort);
            socket.send(packetAck);
            System.out.println(" ACK sent ");
        }
    }
    /*
    * Print ping data to the standard output stream.
    */
    private static void printData(DatagramPacket request) throws Exception
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
        String line = br.readLine();
        // Print host address and data received from it.
        System.out.println(
                "Received from " +
                        request.getAddress().getHostAddress() +
                        ": " +
                        new String(line) );
    }
}
