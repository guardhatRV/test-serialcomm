package test;

import java.util.ArrayList;

public class StateMachine {

    ProtocolFrame pFrame;

    // Constructors
    public StateMachine() {
        pFrame = new ProtocolFrame((byte)0x01, (byte)0x02, (byte)0xbe, (byte)0xef);
    }

    public StateMachine(byte dstAddress,
                        byte command,
                        byte option) {
        pFrame = new ProtocolFrame((byte)0x01, dstAddress, command, option);
    }

    public ArrayList<Byte> getPayload() {
        return(pFrame.getPayload());
    }

	public void buildPayload(ArrayList<Byte> iList) {
        for (byte n : iList) {
            pFrame.addToPayload(n);
            if ((n & 0xFF) == ProtocolFrame.DELIMITER) {
                pFrame.addToPayload(ProtocolFrame.STUFFDATA);
            }
        }
	     // set frame length
	     pFrame.setFrameSize(pFrame.getFrameSize() + getPayload().size());
	}


    public void buildPayload(byte[] iList) {
        for (byte n : iList) {
            pFrame.addToPayload(n);
            if ((n & 0xFF) == ProtocolFrame.DELIMITER) {
                pFrame.addToPayload(ProtocolFrame.STUFFDATA);
            }
        }
        // set frame length
        pFrame.setFrameSize(pFrame.getFrameSize() + getPayload().size());
    }
}