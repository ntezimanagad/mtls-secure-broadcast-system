package com.example.mtlsserver;

import com.example.mtlsserver.model.User;
import org.springframework.stereotype.Service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class UdpBroadcastService {

    private static final int SEND_PORT = 6666;
    private static final int RECEIVE_PORT = 6667;

    public void broadcastUser(User user) {
        try {
            byte[] data = buildMessage(user);

            DatagramSocket socket = new DatagramSocket(SEND_PORT);
            socket.setBroadcast(true);

            InetAddress address = InetAddress.getByName("host.docker.internal");

            DatagramPacket packet =
                    new DatagramPacket(data, data.length, address, RECEIVE_PORT);

            socket.send(packet);
            socket.close();

            System.out.println(
                    "UDP message sent from port " + SEND_PORT +
                    " to port " + RECEIVE_PORT
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] buildMessage(User user) {
        byte[] emailBytes = user.getEmail().getBytes(StandardCharsets.UTF_8);
        byte[] ipBytes = user.getIp().getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(
                4 + emailBytes.length +
                8 +
                4 + ipBytes.length +
                4
        );

        buffer.putInt(emailBytes.length);
        buffer.put(emailBytes);
        buffer.putLong(user.getLastSeen());
        buffer.putInt(ipBytes.length);
        buffer.put(ipBytes);
        buffer.putInt(user.getPort());

        return buffer.array();
    }
}


