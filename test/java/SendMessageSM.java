package test;

import test.SensorRequestOuterClass.*;
import java.io.IOException;


public class SendMessageSM extends StateMachine {

    public SendMessageSM() {
        super();
    }

    public void ping() {
        pFrame.setCommand(ProtocolFrame.PING);
        pFrame.setOption(ProtocolFrame.ZERO);

        pFrame.printFrameBinary();
        //TBD
    }

    public byte[] sendRequest() throws IOException {
        pFrame.setCommand(ProtocolFrame.PROTOBUF);
        pFrame.setOption(ProtocolFrame.ZERO);

        SensorRequestOuterClass.SensorRequest request = SensorRequestOuterClass.SensorRequest.newBuilder()
                .setRequest(SensorRequest.RequestType.GET_DATA)
                .setSensorType(SensorRequest.SensorType.O2)
                .build();

        buildPayload(request.toByteArray());

        byte[] tempArray = pFrame.getByteArray();

        // TEST
        //pFrame.displayFrame(tempArray);

        return(tempArray);
    }
}