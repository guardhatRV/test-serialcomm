package test;

public class ReceiveMessageSM extends StateMachine {

    public enum SMStates {
        VerifyHeader,
        VerifyDestination,
        VerifyCommandOption,
        VerifyStartData,
        ParsePayload,
        VerifyChecksum,
        ErrorState,
        SuccessState;
    }

    SMStates state;
    SMStates nextState;

    int position = 0;

    public ReceiveMessageSM() {
        super();
        state = SMStates.VerifyHeader;
        nextState = SMStates.VerifyHeader;
    }

    public void processFrame(byte[] iFrame) {
        boolean keepParsing = true;
        boolean retVal = true;
        short calcChecksum = 0;

        while((keepParsing) && (position < iFrame.length)) {

            //System.out.printf("State: [%s], LastState: [%s]. RetVal [%b]\n", nextState.toString(), state.toString(), retVal);

            if(retVal == false) {
                state = SMStates.ErrorState;
            } else {
                state = nextState;
            }

            // Debug.. Current byte being parsed
            //System.out.println(String.format("### Debug ### Next Byte: %02x ", iFrame[position]));

            switch(state) {
            case VerifyHeader:
                setNextState(SMStates.VerifyDestination);
                retVal = checkHeader(iFrame);
                setPosition(position + 2);
                break;
            case VerifyDestination:
                setNextState(SMStates.VerifyCommandOption);
                retVal = verifyDestination(iFrame);
                setPosition(position + 2);
                break;
            case VerifyCommandOption:
                setNextState(SMStates.VerifyStartData);
                retVal = verifyCommand(iFrame);
                setPosition(position + 2);
                break;
            case VerifyStartData:
                setNextState(SMStates.ParsePayload);
                retVal = checkStartData(iFrame);
                setPosition(position + 2);
                break;
            case ParsePayload:
                setNextState(SMStates.VerifyChecksum);
                retVal = parsePayload(iFrame);
                break;
            case VerifyChecksum:
                setNextState(SMStates.SuccessState);
                retVal = verifyChecksum(iFrame, calcChecksum);
                break;
            case ErrorState:
                keepParsing = false;
                System.out.println("Invaid protocol frame received. Parsing failed");
                break;
            case SuccessState:
                keepParsing = false;
                System.out.println("Frame successfully parsed");
                break;
            default:
                keepParsing = false;
                System.out.println("Invalid input received. Aborting parse operation");
                break;
            }
        }
    }

    private void setPosition(int iPos) {
        position = iPos;
    }

    private void setNextState(SMStates iState) {
        this.nextState = iState;
    }

    private boolean checkHeader(byte[] iArray) {
        if((iArray[position] == ProtocolFrame.DELIMITER) && (iArray[position+1] == ProtocolFrame.STARTMESSAGE)) {
            return true;
        }
        return false;
    }

    private boolean verifyDestination(byte[] iArray) {
        if(iArray[position] == ProtocolFrame.MASTER) {
            pFrame.setDestinationAddress(iArray[position]);
            pFrame.setSourceAddress(iArray[position+1]);
            return true;
        }
        return false;
    }

    private boolean verifyCommand(byte[] iArray) {
        // Copy the data for now.. We will need to check of the command type indicated protobuf
        // .. else this is a native command
        pFrame.setCommand(iArray[position]);
        pFrame.setOption(iArray[position + 1]);
        return true;
    }

    private boolean checkStartData(byte[] iArray) {
        if((iArray[position] == ProtocolFrame.DELIMITER) && (iArray[position+1] == ProtocolFrame.STARTDATA)) {
            return true;
        }
        return false;
    }

    private boolean parsePayload(byte[] iArray) {
        // This is very hookey and error prone. Change this at the first instance possible
        int frameLeft = iArray.length - position - 3;
        for(int i = 0; i < frameLeft;) {
            // check for delimiter
            if(iArray[position] == ProtocolFrame.DELIMITER) {
                // Verify if this is end of data or we need to escape it.
                if(checkEndData(iArray) == true) {
                    // End of payload data. break and exit
                    System.out.println("END OF DATA");
                    break;
                } else if(checkStuffedData(iArray) == true) {
                    // we have stuff data. Add only escaped byte to payload
                    pFrame.addToPayload(iArray[position]);
                    setPosition(position + 2);
                    i += 2;
                } else {
                    // Illegal sequence found.. exit
                    return(false);
                }
            } else {
                pFrame.addToPayload(iArray[position]);
                setPosition(position + 1);
                i++;
            }
        }
        return true;
    }

    private boolean checkEndData(byte[] iArray) {
        if((iArray[position] == ProtocolFrame.DELIMITER) && (iArray[position+1] == ProtocolFrame.ENDDATA)) {
            return true;
        }
        return false;
    }

    private boolean checkStuffedData(byte[] iArray) {
        if((iArray[position] == ProtocolFrame.DELIMITER) && (iArray[position+1] == ProtocolFrame.STUFFDATA)) {
            return true;
        }
        return false;
    }

    private boolean verifyChecksum(byte[] iArray, short iChecksum) {
        short msb = (short) (iArray[position] << 8 );
        short lsb = iArray[position+1];
        pFrame.setChecksum(iChecksum);

        if((msb & lsb) == iChecksum) {
            return true;
        }
        return false;
    }

    public void processPayload() throws Exception {

        byte[] tempArray = new byte[pFrame.getPayload().size()];
        for(int i = 0; i < pFrame.getPayload().size(); i++) {
            tempArray[i] = pFrame.getPayload().get(i).byteValue();
        }

        SensorDataOuterClass.SensorData sensorData =  SensorDataOuterClass.SensorData.parseFrom(tempArray);
        if(sensorData.hasSensors()) {
            SensorDataOuterClass.Sensor sensor = sensorData.getSensors();
            if (sensor.hasValue()) {
                System.out.println("Sensor Type: " + sensor.getSensorType());
                System.out.println(("Sensor Value: " + sensor.getValue()));
            }
        }
    }
}
