import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class SKJDAS {
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new RuntimeException("usage: SKJDAS <port> <number>");
        }

        int port, number;

        boolean masterMode = true;

        try {
            port = Integer.parseInt(args[0]);
            number = Integer.parseInt(args[1]);
        } catch (NumberFormatException _) {
            throw new RuntimeException("usage: SKJDAS <int port> <int number>");
        }

        System.out.println("Starting a program, port: " + port + ", number: " + number);

        DatagramPacket packet = null;
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket(port);
            System.out.println("Entering master mode");
        } catch (SocketException _) {
            masterMode = false;
            System.out.println("Entering slave mode");
        }

        if (masterMode) {
            ArrayList<Integer> mem = new ArrayList<>();

            packet = new DatagramPacket(new byte[4],4);
            while (true) {
                try {
                    socket.receive(packet);
                    byte[] data = packet.getData();

                    while (data[0] == 0 && data.length > 1) {
                        byte[] buf = data;
                        data = new byte[buf.length-1];

                        for (int i = 0; i < data.length; i++) {
                            data[i] = buf[i+1];
                        }
                    }

                    int val = new BigInteger(data).intValueExact();
                    System.out.println(Arrays.toString(data));
                    if (val == 0) {
                        mem.add(number);
                        int avg = getAverage(mem);
                        System.out.println("Average: "  + avg);
                        //todo broadcast
                    } else if (val == -1 ) {
                        System.out.println(val);
                        //todo broadcast
                        socket.close();
                        break;
                    } else {
                        System.out.println(val);
                        mem.add(val);
                    }
                } catch (IOException _) {
                    System.out.println("IOException while receiving a message");
                }
            }

        } else {

            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                throw new RuntimeException("Could not create a random socket");
            }
            byte[] data = BigInteger.valueOf(number).toByteArray();
            System.out.println(Arrays.toString(data));

            if (data.length < 4) {
                byte[] buf = data;
                data = new byte[4];

                for (int i = 0; i < buf.length; i++) {
                    data[data.length-1-i] = buf[i];
                }
            }

            System.out.println(Arrays.toString(data));

            try {
                socket.send(new DatagramPacket(data, data.length, new InetSocketAddress("localhost", port)));
            } catch (IOException e) {
                throw new RuntimeException("Could not send a message");
            }

        }

        System.out.println("Exited");
    }

    public static int getAverage (ArrayList<Integer> list) {
        int avg = 0;

        for (Integer i : list) {
            avg += i;
        }

        return avg/list.size();
    }
}
