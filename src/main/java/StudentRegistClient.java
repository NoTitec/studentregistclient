import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class StudentRegistClient {
    public static void main(String[] args) throws IOException {
        InetAddress addr=InetAddress.getLocalHost();
        String localname=addr.getHostName();
        String localip=addr.getHostAddress();
        System.out.println(localname+localip);
        Socket s=new Socket(localip,3000);
        System.out.println("connect");
        InputStream is=s.getInputStream();
        BufferedInputStream bis=new BufferedInputStream(is);
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        Scanner sc=new Scanner(System.in);
        OutputStream os=s.getOutputStream();
        BufferedOutputStream bos=new BufferedOutputStream(os);

        boolean programexitflag=false;
        Rprotocol proto = new Rprotocol();
        byte[] buf = proto.getPacket();

        while(!programexitflag){
            bis.read(buf);//서버에서 패킷수신 buf에 저장
            System.out.println("수신");
            int packetType=buf[0];//수신타입
            System.out.print("type "+packetType);
            int packetstuType=buf[1];//수신학생타입
            System.out.print(" stutype "+packetstuType);
            int packetCode=buf[2];//수신코드
            System.out.println(" code "+packetCode);
            proto.getPacket(packetType,packetstuType,packetCode);

            if(packetstuType==-1){//normal protocol
                byte[] payload;
                byte[] sendData;
                int index;
                byte[] payloadSize;
                switch(packetType){
                    case Rprotocol.ACCOUNT_INFO_RESULT:
                        switch(packetCode){
                            case Rprotocol.STU_LOGIN_SUCCESS_CODE:
                                System.out.println("로그인성공");
                                //메뉴선택하고 선택메뉴에따라 요청패킷서버전달
                               break;

                            case Rprotocol.STU_LOGIN_FAIL_CODE:
                                programexitflag=true;
                                proto=new Rprotocol(Rprotocol.ACCOUNT_INFO_RESULT,Rprotocol.PT_UNDEFINED,Rprotocol.STU_LOGIN_FAIL_CODE);
                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                        }
                        break;
                    case Rprotocol.ACCOUNT_INFO_REQ:
                        switch(packetCode) {
                            case Rprotocol.WHO_INFO_REQ:
                            System.out.println("서버가 직책확인 요청 패킷 보냄");
                            System.out.println("1.관리자 2.교수 3.학생");
                            String rollinput= userInput.readLine();

                            payload = rollinput.getBytes();

                            sendData = new byte[payload.length + 200];

                            index = 0;
                            payloadSize = proto.IntToBytes(payload.length);//길이정보넣기
                            sendData[0] = (byte) payloadSize[0];
                            sendData[1] = (byte) payloadSize[1];
                            sendData[2] = (byte) payloadSize[2];
                            sendData[3] = (byte) payloadSize[3];
                            index = 4;
                                for(int i = 0; i < payload.length; i++){//실제자료넣기
                                    sendData[index] = payload[i];
                                    index++;
                                }
                            proto.setPacket_type_and_code(Rprotocol.ACCOUNT_INFO_ANS, Rprotocol.PT_UNDEFINED, Rprotocol.WHO_INFO_ANS);

                            proto.setPacket(Rprotocol.ACCOUNT_INFO_ANS, Rprotocol.PT_UNDEFINED, Rprotocol.WHO_INFO_ANS, sendData);

                            bos.write(proto.getPacket());
                            bos.flush();
                            break;
                            case Rprotocol.STU_ID_PWD_REQ_CODE:
                                System.out.println("서버가 아이디,비밀번호 요청 패킷 보냄");
                                String id= userInput.readLine();
                                String pwd=userInput.readLine();
                                byte[] payload1=id.getBytes();
                                byte[] payload2=pwd.getBytes();
                                sendData=new byte[payload1.length+payload2.length+200];//보낼 byte배열
                                index=0;
                                payloadSize=proto.IntToBytes(id.getBytes().length);//id길이정보넣기
                                sendData[0] = (byte) payloadSize[0];
                                sendData[1] = (byte) payloadSize[1];
                                sendData[2] = (byte) payloadSize[2];
                                sendData[3] = (byte) payloadSize[3];
                                index = 4;
                                for(int i = 0; i < id.getBytes().length; i++){//id자료넣기
                                    sendData[index] = payload1[i];
                                    index++;
                                }

                                payloadSize=proto.IntToBytes(pwd.getBytes().length);
                                sendData[index] = (byte) payloadSize[0];
                                sendData[index+1] = (byte) payloadSize[1];
                                sendData[index+2] = (byte) payloadSize[2];
                                sendData[index+3] = (byte) payloadSize[3];
                                index +=4;
                                for(int i = 0; i < pwd.getBytes().length; i++){
                                    sendData[index] = payload2[i];
                                    index++;
                                }
                                proto.setPacket_type_and_code(Rprotocol.ACCOUNT_INFO_ANS, Rprotocol.PT_UNDEFINED, Rprotocol.STU_ID_PWD_ANS_CODE);

                                proto.setPacket(Rprotocol.ACCOUNT_INFO_ANS, Rprotocol.PT_UNDEFINED, Rprotocol.STU_ID_PWD_ANS_CODE, sendData);

                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                        }
                        break;

                }
            }
            else{//student protocol

            }
        }
        s.close();
    }
}
