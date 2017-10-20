package test;

import gnu.io.CommPortIdentifier;
import gnu.io.NRSerialPort;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public enum NRJavaSerialTest {
    ;

    private static final String TTY_SERIAL_PORT_NAME = "/dev/tty.usbserial-A50285BI";
    public static final int BAUD = 115200;


    private static boolean hasPort(final String portName) {
        final List<CommPortIdentifier> portIdentifiers = Collections.list(CommPortIdentifier.getPortIdentifiers());

        return portIdentifiers.stream()
                              .anyMatch(portIdentifier -> Objects.equals(portIdentifier.getName(), portName));

    }

    /**
     * @param args
     */
    public static void main (final String... args)  throws Exception {
        System.out.println("Starting Test..");

        if (hasPort(TTY_SERIAL_PORT_NAME)) {
            doSendAndReceive();
        }
    }

    private static void doSendAndReceive() throws Exception {
        final NRSerialPort port = new NRSerialPort(TTY_SERIAL_PORT_NAME, BAUD);

        if (port.connect()) {
            try (DataInputStream inputStream = new DataInputStream(port.getInputStream());
                 DataOutputStream outputStream = new DataOutputStream(port.getOutputStream())) {

                SendMessageSM sendSM = new SendMessageSM();
                byte[] tempArray = sendSM.sendRequest();

                System.out.println("Binary data being sent to the board");
                displayFrame(tempArray);

                outputStream.write(tempArray);
                outputStream.flush();

                Thread.sleep(500);

                System.out.println("Receive Data: " + inputStream.available());

                byte[] receiveFrame = new byte[inputStream.available()];
                inputStream.read(receiveFrame);
                displayFrame(receiveFrame);
                System.out.println();

                ReceiveMessageSM receiveSM = new ReceiveMessageSM();
                receiveSM.processFrame(receiveFrame);
                receiveSM.processPayload();

            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n");

        port.disconnect();
    }

    private static void displayFrame(byte[] inputArray) {
        int newline = 0;
        for (byte n : inputArray) {
            System.out.print(String.format("0x%02x ", n));
            if (++newline >= 8) {
                System.out.println();
                newline = 0;
            }
        }
        System.out.println();
    }

}
