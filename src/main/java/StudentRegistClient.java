import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class StudentRegistClient {

    public static void main(String[] args) throws IOException {
        InetAddress addr = InetAddress.getLocalHost();
        String localname = addr.getHostName();
        String localip = addr.getHostAddress();
        System.out.println(localname + localip);
        Socket s = new Socket(localip, 3000);
        System.out.println("connect");
        InputStream is = s.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        Scanner sc = new Scanner(System.in);
        OutputStream os = s.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);

        Menu m = new Menu();
        boolean programexitflag = false;
        Rprotocol proto = new Rprotocol();
        byte[] buf = proto.getPacket();

        while (!programexitflag) {
            bis.read(buf);//서버에서 패킷수신 buf에 저장
            System.out.println("수신");
            int packetType = buf[0];//수신타입
            System.out.print("type " + packetType);
            int packetstuType = buf[1];//수신학생타입
            System.out.print(" stutype " + packetstuType);
            int packetCode = buf[2];//수신코드
            System.out.println(" code " + packetCode);
            proto.getPacket(packetType, packetstuType, packetCode);
            byte[] payload;
            byte[] sendData;
            int pos;
            byte[] payloadSize;
            int selectmenunumber;
            int rcvdatacount;
            if (packetstuType == -1) {//normal protocol

                switch (packetType) {
                    case Rprotocol.ACCOUNT_INFO_RESULT:
                        switch (packetCode) {
                            case Rprotocol.STU_LOGIN_SUCCESS_CODE:
                                m.menuprint();//메뉴출력

                                while (true) {
                                    selectmenunumber = sc.nextInt();
                                    if (selectmenunumber == 1) {
                                        proto = new Rprotocol(Rprotocol.PT_UNDEFINED, Rprotocol.REGIST_REQ, Rprotocol.REGIST_REQ_CODE);
                                        bos.write(proto.getPacket());
                                        bos.flush();
                                        break;
                                    } else if (selectmenunumber == 2) {
                                        proto = new Rprotocol(Rprotocol.PT_UNDEFINED, Rprotocol.REGIST_REQ, Rprotocol.REGIST_CANCEL_CODE);
                                        bos.write(proto.getPacket());
                                        bos.flush();
                                        break;
                                    } else if (selectmenunumber == 3) {
                                        proto = new Rprotocol(Rprotocol.PT_UNDEFINED, Rprotocol.STU_INFO_UPDATE_REQ, Rprotocol.PWD_UPDATE_REQ_CODE);
                                        bos.write(proto.getPacket());
                                        bos.flush();
                                        break;
                                    } else if (selectmenunumber == 4) {
                                        proto = new Rprotocol(Rprotocol.PT_UNDEFINED, Rprotocol.CREATE_SUBJECT_INFO_REQ, Rprotocol.SIMPLE_LOOK_CODE);
                                        bos.write(proto.getPacket());
                                        bos.flush();
                                        break;
                                    } else if (selectmenunumber == 5) {
                                        proto = new Rprotocol(Rprotocol.PT_UNDEFINED, Rprotocol.CREATE_SUBJECT_INFO_REQ, Rprotocol.SUBJECT_PLAN_REQ_CODE);
                                        bos.write(proto.getPacket());
                                        bos.flush();
                                        break;
                                    } else if (selectmenunumber == 6) {
                                        proto = new Rprotocol(Rprotocol.PT_UNDEFINED, Rprotocol.MY_TIMETABLE_REQ, Rprotocol.PT_UNDEFINED);
                                        bos.write(proto.getPacket());
                                        bos.flush();
                                        break;
                                    } else {
                                        System.out.println("you input wrong menu number try again");
                                    }
                                }
                                break;

                            case Rprotocol.STU_LOGIN_FAIL_CODE:
                                programexitflag = true;
                                proto = new Rprotocol(Rprotocol.ACCOUNT_INFO_RESULT, Rprotocol.PT_UNDEFINED, Rprotocol.STU_LOGIN_FAIL_CODE);
                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                        }
                        break;
                    case Rprotocol.ACCOUNT_INFO_REQ:
                        switch (packetCode) {
                            case Rprotocol.WHO_INFO_REQ:
                                System.out.println("서버가 직책확인 요청 패킷 보냄");
                                System.out.println("1.관리자 2.교수 3.학생");
                                String rollinput = userInput.readLine();

                                payload = rollinput.getBytes();

                                sendData = new byte[payload.length + 200];

                                pos = 0;
                                payloadSize = proto.IntToBytes(payload.length);//길이정보넣기
                                sendData[0] = (byte) payloadSize[0];
                                sendData[1] = (byte) payloadSize[1];
                                sendData[2] = (byte) payloadSize[2];
                                sendData[3] = (byte) payloadSize[3];
                                pos = 4;
                                for (int i = 0; i < payload.length; i++) {//실제자료넣기
                                    sendData[pos] = payload[i];
                                    pos++;
                                }
                                proto.setPacket_type_and_code(Rprotocol.ACCOUNT_INFO_ANS, Rprotocol.PT_UNDEFINED, Rprotocol.WHO_INFO_ANS);

                                proto.setPacket(Rprotocol.ACCOUNT_INFO_ANS, Rprotocol.PT_UNDEFINED, Rprotocol.WHO_INFO_ANS, sendData);

                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                            case Rprotocol.STU_ID_PWD_REQ_CODE:
                                System.out.println("서버가 아이디,비밀번호 요청 패킷 보냄");
                                String id = userInput.readLine();
                                String pwd = userInput.readLine();
                                byte[] payload1 = id.getBytes();
                                byte[] payload2 = pwd.getBytes();
                                sendData = new byte[payload1.length + payload2.length + 200];//보낼 byte배열
                                pos = 0;
                                payloadSize = proto.IntToBytes(id.getBytes().length);//id길이정보넣기
                                sendData[0] = (byte) payloadSize[0];
                                sendData[1] = (byte) payloadSize[1];
                                sendData[2] = (byte) payloadSize[2];
                                sendData[3] = (byte) payloadSize[3];
                                pos = 4;
                                for (int i = 0; i < id.getBytes().length; i++) {//id자료넣기
                                    sendData[pos] = payload1[i];
                                    pos++;
                                }

                                payloadSize = proto.IntToBytes(pwd.getBytes().length);
                                sendData[pos] = (byte) payloadSize[0];
                                sendData[pos + 1] = (byte) payloadSize[1];
                                sendData[pos + 2] = (byte) payloadSize[2];
                                sendData[pos + 3] = (byte) payloadSize[3];
                                pos += 4;
                                for (int i = 0; i < pwd.getBytes().length; i++) {
                                    sendData[pos] = payload2[i];
                                    pos++;
                                }
                                proto.setPacket_type_and_code(Rprotocol.ACCOUNT_INFO_ANS, Rprotocol.PT_UNDEFINED, Rprotocol.STU_ID_PWD_ANS_CODE);

                                proto.setPacket(Rprotocol.ACCOUNT_INFO_ANS, Rprotocol.PT_UNDEFINED, Rprotocol.STU_ID_PWD_ANS_CODE, sendData);

                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                        }
                        break;

                }
            } else {//student protocol
                switch(packetstuType){
                    case Rprotocol.CREATE_SUBJECT_INFO_ANS:
                        switch (packetCode){
                            case Rprotocol.CREAT_SUBJECT_GRADE_CODE:
                                System.out.println("서버가보낸 나의 학년과 같은 교과목목록 출력");
                                pos=3;
                                rcvdatacount=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));
                                System.out.println("개설교과목:"+rcvdatacount+"개");
                                pos+=4;

                                for(int i=0;i<rcvdatacount;i++){
                                    //one code 길이 추출해 저장
                                    int codelength=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));//4byte 길이정보 int 변환
                                    pos+=4;//4byte 읽었으므로 pos를 4만큼 증가

                                    byte[] word = Arrays.copyOfRange(buf, pos, pos+codelength);//추출한길이만큼읽어 코드추출
                                    String onecode = new String(word);//추출 word를  string으로 변환하여 저장

                                    System.out.println("과목코드:"+onecode);
                                    pos+=codelength;// one code길이만큼 읽었으므로 pos를 one word 길이만큼 증가

                                    //one name 길이 추출해 저장
                                    int namelength=proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos+4));//4byte 길이정보 int 변환
                                    pos+=4;//4byte 읽었으므로 pos를 4만큼 증가
                                    word = Arrays.copyOfRange(buf, pos, pos+namelength);//추출한길이만큼읽어 코드추출
                                    String onename = new String(word);//추출 word를  string으로 변환하여 저장

                                    System.out.println("과목이름:"+onename);
                                    pos+=namelength;// one word길이만큼 읽었으므로 pos를 one word 길이만큼 증가
                                }
                                System.out.println("수강신청하고자하는 과목 코드를 입력하세요");
                                pos=0;
                                String selectsubjectcode=sc.next();
                                payload=new byte[50];//선택과목 코드길이,코드저장할배열
                                int selectsubjectcodelen=selectsubjectcode.length();
                                System.arraycopy(proto.intto4byte(selectsubjectcodelen),0,payload,pos,proto.intto4byte(selectsubjectcodelen).length);
                                pos+=4;
                                System.arraycopy(selectsubjectcode.getBytes(),0,payload,pos,selectsubjectcode.getBytes().length);

                                proto.setPacket_type_and_code(Rprotocol.ACCOUNT_INFO_ANS, Rprotocol.PT_UNDEFINED, Rprotocol.STU_ID_PWD_ANS_CODE);

                                proto.setPacket(Rprotocol.PT_UNDEFINED, Rprotocol.SEL_SUBJECT_ANS, Rprotocol.SELECT_REGIST_SUBJECT_CODE, payload);

                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                        }
                        break;
                        //---------------------------------------------
                    case Rprotocol.REGIST_RESULT:
                        switch (packetCode){
                            case Rprotocol.REGIST_SUCESS_CODE:
                                System.out.println("수강신청완료");
                                proto.setPacket_type_and_code(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                                proto.setPacket(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                            case Rprotocol.NOT_REGIST_DAY_CODE:
                                System.out.println("수강신청기간아님");
                                proto.setPacket_type_and_code(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                                proto.setPacket(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                            case Rprotocol.DUP_TIME_CODE:
                                System.out.println("수강신청요청한 과목이 내 시간표와 중복됨");
                                proto.setPacket_type_and_code(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                                proto.setPacket(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                            case Rprotocol.MAX_STU_CODE:
                                System.out.println("수강인원초과");
                                proto.setPacket_type_and_code(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                                proto.setPacket(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                                bos.write(proto.getPacket());
                                bos.flush();
                        }
                        break;
                        //---------------------------------------------------
                    case Rprotocol.MY_REGIST_ANS:
                        switch (packetCode){
                            case Rprotocol.SUBJECT_CODE_INFO_CODE:
                                System.out.println("서버가보낸 내가 수강하는 교과목목록 출력");
                                pos=3;
                                rcvdatacount=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));
                                System.out.println("수강교과목:"+rcvdatacount+"개");
                                pos+=4;

                                for(int i=0;i<rcvdatacount;i++){
                                    //one code 길이 추출해 저장
                                    int codelength=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));//4byte 길이정보 int 변환
                                    pos+=4;//4byte 읽었으므로 pos를 4만큼 증가

                                    byte[] word = Arrays.copyOfRange(buf, pos, pos+codelength);//추출한길이만큼읽어 코드추출
                                    String onecode = new String(word);//추출 word를  string으로 변환하여 저장

                                    System.out.println("과목코드:"+onecode);
                                    pos+=codelength;// one code길이만큼 읽었으므로 pos를 one word 길이만큼 증가

                                    //one name 길이 추출해 저장
                                    int namelength=proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos+4));//4byte 길이정보 int 변환
                                    pos+=4;//4byte 읽었으므로 pos를 4만큼 증가
                                    word = Arrays.copyOfRange(buf, pos, pos+namelength);//추출한길이만큼읽어 코드추출
                                    String onename = new String(word);//추출 word를  string으로 변환하여 저장

                                    System.out.println("과목이름:"+onename);
                                    pos+=namelength;// one word길이만큼 읽었으므로 pos를 one word 길이만큼 증가
                                }
                                System.out.println("수강취소하고자하는 과목 코드를 입력하세요");
                                pos=0;
                                String selectsubjectcode=sc.next();
                                payload=new byte[50];//선택과목 코드길이,코드저장할배열
                                int selectsubjectcodelen=selectsubjectcode.length();
                                System.arraycopy(proto.intto4byte(selectsubjectcodelen),0,payload,pos,proto.intto4byte(selectsubjectcodelen).length);
                                pos+=4;
                                System.arraycopy(selectsubjectcode.getBytes(),0,payload,pos,selectsubjectcode.getBytes().length);

                                proto.setPacket(Rprotocol.PT_UNDEFINED, Rprotocol.SEL_SUBJECT_ANS, Rprotocol.SELECT_CANCLE_SUBJECT_CODE, payload);

                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                        }
                        break;
                        //-------------------------------------
                    case Rprotocol.REGIST_CANSEL_RESULT:
                        System.out.println("수강취소성공");
                        proto.setPacket(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                        bos.write(proto.getPacket());
                        bos.flush();
                        break;
                        //--------------------------------------

                }
            }
        }
        s.close();
    }

}



