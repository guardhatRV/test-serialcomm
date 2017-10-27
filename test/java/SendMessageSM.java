package test;

import test.SensorRequestOuterClass.*;
import java.io.IOException;


public class SendMessageSM extends StateMachine {

    public SendMessageSM() {
        super();
    }

    public byte[] sendRequest() throws IOException {
        pFrame.setCommand(ProtocolFrame.PROTOBUF);
        pFrame.setOption(ProtocolFrame.ZERO);

        SensorRequestOuterClass.SensorRequest request = SensorRequestOuterClass.SensorRequest.newBuilder()
                .setRequest(SensorRequest.RequestType.GET_DATA)
                .setSensorType(SensorRequest.SensorType.ALL)
                .build();

        buildPayload(request.toByteArray());

        byte[] tempArray = pFrame.getByteArray();

        return(tempArray);
    }
}