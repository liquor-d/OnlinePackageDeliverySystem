package org.example.IO;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessageV3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class MessageHelper {

    // send
    public static <T extends GeneratedMessageV3> void sendRequset(T request, OutputStream out) throws IOException {
        try{
            byte[] data = request.toByteArray();
            CodedOutputStream cos = CodedOutputStream.newInstance(out);
            cos.writeUInt32NoTag(data.length);
            cos.writeRawBytes(data);
            cos.flush();
        }catch (IOException e){
            System.err.println("sendRequset: " + e.toString());
        }
    }

    // recv
    public static <T extends GeneratedMessageV3.Builder<?>> void recvResponse(T response, InputStream in) throws IOException {
        try{
            CodedInputStream cis = CodedInputStream.newInstance(in);
            int size = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(size);
            response.mergeFrom(cis);
            cis.popLimit(oldLimit);
        }catch (IOException e){
            System.err.println("recvResponse: " + e.toString());
        }
    }

}
