package test;

import java.util.ArrayList;

public class ProtocolFrame {

	public static final byte DELIMITER     = 0x10;
	public static final byte STUFFDATA     = (byte)0xef;
	public static final byte STARTMESSAGE  = 0x01;
	public static final byte STARTDATA     = 0x02;
	public static final byte ENDDATA       = 0x04;
	public static final byte ACK           = 0x06;
	public static final byte NACK          = 0x15;
	public static final byte MASTER        = 0x01;
	public static final byte PING		   = (byte)0x80;
	public static final byte ZERO          = 0x00;
	public static final byte PROTOBUF      = 0x20;


	private final byte startMsgDelimiter;	// Fixed value 0x10 - Delimiter
	private final byte startMessage;		// Fixed value 0x01 - Start of Message
	private byte destinationAddress;		// Destination Address
	private byte sourceAddress;				// Source Address
	private byte command;					// Command
	private byte option;					// Options
	private final byte startDataDelimiter;	// Fixed value 0x10 - Delimiter for start of data
	private final byte startData;			// Fixed value 0x02 - start of data buffer
	private ArrayList<Byte> payload;		// Array of frame data
	private final byte endDataDelimiter;	// Fixed value 0x10 - Delimiter for end of data
	private final byte endData;				// Fixed value 0x04 - End of data buffer
	public short checksum;					// 16Bit Checksum Value
	private int frameSize;                  // Total size of the frame to be passed to the serial interface
    private boolean hasPayload;             // Flag to determine if we have a payload or not
    private Integer sumValue;               // Running total sum for 2's compliment

	public ProtocolFrame() {
		startMsgDelimiter 	= DELIMITER;
		startMessage 		= (byte)0x01;
		setDestinationAddress((byte)0x00);
		setSourceAddress((byte)0x01);
		setCommand((byte)0xff);
		setOption((byte)0xff);
		startDataDelimiter	= DELIMITER;
		startData			= (byte)0x02;
		payload 			=  new ArrayList<Byte> ();
		endDataDelimiter	= DELIMITER;
		endData				= (byte)0x04;
		checksum			= 0x0000;
		frameSize           = 11;
		hasPayload          = false;
		sumValue            = (startMsgDelimiter + startMessage + sourceAddress + startDataDelimiter +
                                startData + endDataDelimiter + endData);
	}

	public ProtocolFrame(byte srcAddress,
	                     byte dstAddress,
	                     byte command,
	                     byte option) {
	    this();
	    setSourceAddress(srcAddress);
	    setDestinationAddress(dstAddress);
	    setCommand(command);
	    setOption(option);
	}

	public ArrayList<Byte> getPayload() {
		return(payload);
	}

	public void addToPayload(byte iValue) {
	    payload.add(iValue);
	    calculateChecksum(iValue);
	}

	public void setChecksum(short iValue) {
		checksum = iValue;
	}

	public byte getStartMsgDelimiter() {
		return startMsgDelimiter;
	}

	public byte getStartMessage() {
		return startMessage;
	}

	public byte getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(byte destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public byte getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(byte sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public byte getCommand() {
		return command;
	}

	public void setCommand(byte command) {
		this.command = command;
	}

	public byte getOption() {
		return option;
	}

	public void setOption(byte option) {
		this.option = option;
	}

	public byte getStartDataDelimiter() {
		return startDataDelimiter;
	}

	public byte getStartData() {
		return startData;
	}

	public byte getEndDataDelimiter() {
		return endDataDelimiter;
	}

	public byte getEndData() {
		return endData;
	}

    public int getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    public void calculateChecksum(short iValue) {
        setChecksum((short) (checksum ^ iValue));
    }

    public void calculate2sCompliment() {
        sumValue = ~(sumValue);
	    checksum = sumValue.shortValue();
	    checksum += 1;
    }

    public boolean getHasPayload() {
        return hasPayload;
    }

    public void setHasPayload(boolean hasPayload) {
        this.hasPayload = hasPayload;
    }

    public void setSumValue(int iValue) {
	    sumValue += iValue;
    }

	void printFrame() {
        System.out.println("Frame Size: " + getFrameSize());

        System.out.println("Protocol Frame");
	    System.out.println("--------------");
	    System.out.println("Start of frame :" + Integer.toHexString(startMsgDelimiter & 0xFF) + " " + Integer.toHexString(startMessage & 0xFF));
	    System.out.println("Destination    :" + Integer.toHexString(destinationAddress & 0xFF));
	    System.out.println("Source         :" + Integer.toHexString(sourceAddress & 0xFF));
	    System.out.println("Command        :" + Integer.toHexString(command & 0xFF));
	    System.out.println("Option         :" + Integer.toHexString(option & 0xFF));
	    System.out.println("Start of msg   :" + Integer.toHexString(startDataDelimiter & 0xFF) + " " + Integer.toHexString(startData & 0xFF));
	    System.out.printf("Payload Size   :%d\n", getPayload().size());
	    System.out.print("Payload        :");
	    int newline = 0;
	    for(byte n : getPayload()) {
            System.out.print(String.format("0x%02X ", n));
            if(++newline >= 8) {
                System.out.println();
                System.out.printf("%16s", " ");
                newline = 0;
            }
        }
	    System.out.println();
	    System.out.println("End of msg     :" + Integer.toHexString(endDataDelimiter & 0xFF) + " " + Integer.toHexString(endData & 0xFF));
	    System.out.println("Checksum       :" + Integer.toHexString(checksum & 0xFFFF));
	    System.out.println();
	}

	public void printFrameBinary() {
	    System.out.println("Protocol Frame Binary");
	    System.out.println("---------------------");
	    System.out.printf("%02x %02x %02x %02x %02x %02x %02x %02x\n",
	            startMsgDelimiter, startMessage, destinationAddress, sourceAddress,
	            command, option, startDataDelimiter, startData);
	    int newline = 0;
        for(byte n : getPayload()) {
            System.out.print(String.format("%02x ", n));
            if(++newline >= 8) {
                System.out.println();
                newline = 0;
            }
        }
        System.out.printf("%02x ", endDataDelimiter);
        if(++newline >= 8) { System.out.println(); newline = 0; }
        System.out.printf("%02x ", endData);
        System.out.println();
        System.out.printf("Checksum: %04x\n", checksum);
        System.out.println();
	}


    public byte[] getByteArray(){
        byte[] retArray = new byte[getFrameSize()];
        retArray[0] = startMsgDelimiter;
        retArray[1] = startMessage;
        retArray[2] = destinationAddress;
        retArray[3] = sourceAddress;
        retArray[4] = command;
        retArray[5] = option;
        retArray[6] = startDataDelimiter;
        retArray[7] = startData;

        setSumValue(destinationAddress);
        setSumValue(command);
        setSumValue(option);

        int i = 8;
        for(int j = 0; (i < getFrameSize()) && (j < getPayload().size()); i++, j++) {
            retArray[i] = getPayload().get(j);
            setSumValue(retArray[i]);
        }

        retArray[i] = endDataDelimiter;
        retArray[i+1] = endData;
        calculate2sCompliment();
       // retArray[i+2] = (byte) (checksum >> 8);
        retArray[i+2] = (byte) (checksum & 0xFF);

        return(retArray);
    }

    public void displayFrame(byte[] inputArray){
        int newline = 0;
        for (byte n : inputArray) {
            System.out.print(String.format("%02x ", n));
            if (++newline >= 8) {
                System.out.println();
                newline = 0;
            }
        }
        System.out.println();
    }

}
