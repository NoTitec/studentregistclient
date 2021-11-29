import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

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
            int pos;//payload나 sendData 배열세팅위한 인덱싱변수
            byte[] payloadSize;
            int selectmenunumber;//선택메뉴값저장
            int rcvdatacount;//받은데이터개수저장
            String selectsubjectcode;//과목코드입력문자열저장
            int selectsubjectcodelen;//과목코드바이트배열길이저장
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
                                selectsubjectcode=sc.next();
                                payload=new byte[50];//선택과목 코드길이,코드저장할배열
                                selectsubjectcodelen=selectsubjectcode.length();
                                System.arraycopy(proto.intto4byte(selectsubjectcodelen),0,payload,pos,proto.intto4byte(selectsubjectcodelen).length);
                                pos+=4;
                                System.arraycopy(selectsubjectcode.getBytes(),0,payload,pos,selectsubjectcode.getBytes().length);

                                proto.setPacket_type_and_code(Rprotocol.ACCOUNT_INFO_ANS, Rprotocol.PT_UNDEFINED, Rprotocol.STU_ID_PWD_ANS_CODE);

                                proto.setPacket(Rprotocol.PT_UNDEFINED, Rprotocol.SEL_SUBJECT_ANS, Rprotocol.SELECT_REGIST_SUBJECT_CODE, payload);

                                bos.write(proto.getPacket());
                                bos.flush();
                                break;

                            case Rprotocol.CREAT_SUBJECT_GRADE_ALL_CODE:
                                System.out.println("서버가보낸 전체 교과목목록 출력");
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

                                    System.out.print("과목코드:"+onecode+" ");
                                    pos+=codelength;// one code길이만큼 읽었으므로 pos를 one word 길이만큼 증가

                                    //one name 길이 추출해 저장
                                    int namelength=proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos+4));//4byte 길이정보 int 변환
                                    pos+=4;//4byte 읽었으므로 pos를 4만큼 증가
                                    word = Arrays.copyOfRange(buf, pos, pos+namelength);//추출한길이만큼읽어 코드추출
                                    String onename = new String(word);//추출 word를  string으로 변환하여 저장

                                    System.out.println("과목이름:"+onename);
                                    pos+=namelength;// one word길이만큼 읽었으므로 pos를 one word 길이만큼 증가
                                }
                                proto.setPacket(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                            case Rprotocol.CREAT_SUBJECT_GRADE_ALL_AND_PLAN_CODE:
                                System.out.println("서버가보낸 전체 교과목목록 출력");
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

                                    System.out.print("과목코드:"+onecode+" ");
                                    pos+=codelength;// one code길이만큼 읽었으므로 pos를 one word 길이만큼 증가

                                    //one name 길이 추출해 저장
                                    int namelength=proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos+4));//4byte 길이정보 int 변환
                                    pos+=4;//4byte 읽었으므로 pos를 4만큼 증가
                                    word = Arrays.copyOfRange(buf, pos, pos+namelength);//추출한길이만큼읽어 코드추출
                                    String onename = new String(word);//추출 word를  string으로 변환하여 저장

                                    System.out.println("과목이름:"+onename);
                                    pos+=namelength;// one word길이만큼 읽었으므로 pos를 one word 길이만큼 증가
                                }
                                System.out.println("강의계획서 조회하고자하는 과목 코드를 입력하세요");
                                pos=0;
                                selectsubjectcode=sc.next();
                                payload=new byte[50];//선택과목 코드길이,코드저장할배열
                                selectsubjectcodelen=selectsubjectcode.length();
                                System.arraycopy(proto.intto4byte(selectsubjectcodelen),0,payload,pos,proto.intto4byte(selectsubjectcodelen).length);
                                pos+=4;
                                System.arraycopy(selectsubjectcode.getBytes(),0,payload,pos,selectsubjectcode.getBytes().length);

                                proto.setPacket(Rprotocol.PT_UNDEFINED, Rprotocol.SEL_SUBJECT_ANS, Rprotocol.SELECT_PLAN_SUBJECT_CODE, payload);

                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                        }
                        break;
                        //---------------------------------------------
                    case Rprotocol.REGIST_RESULT:
                        switch (packetCode){//수강신청 결과받고 메뉴출력요청패킷보냄
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
                                selectsubjectcode=sc.next();
                                payload=new byte[50];//선택과목 코드길이,코드저장할배열
                                selectsubjectcodelen=selectsubjectcode.length();
                                System.arraycopy(proto.intto4byte(selectsubjectcodelen),0,payload,pos,proto.intto4byte(selectsubjectcodelen).length);
                                pos+=4;
                                System.arraycopy(selectsubjectcode.getBytes(),0,payload,pos,selectsubjectcode.getBytes().length);

                                proto.setPacket(Rprotocol.PT_UNDEFINED, Rprotocol.SEL_SUBJECT_ANS, Rprotocol.SELECT_CANCLE_SUBJECT_CODE, payload);

                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                            case Rprotocol.SUBJECT_TIME_INFO_CODE:
                                System.out.println("서버가보낸 내 수강과목 요일,시작시간,종료시간 출력");
                                pos=3;
                                rcvdatacount=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));
                                System.out.println("수강교과목:"+rcvdatacount+"개");
                                pos+=4;
                                List<Mytimetable>monlist=new ArrayList<>();//월요일시간정보담을 list
                                List<Mytimetable>tuslist=new ArrayList<>();//화요일시간정보담을 list
                                List<Mytimetable>wenlist=new ArrayList<>();//수요일시간정보담을 list
                                List<Mytimetable>thrlist=new ArrayList<>();//목요일시간정보담을 list
                                List<Mytimetable>frylist=new ArrayList<>();//금요일시간정보담을 list
                                for(int i=0;i<rcvdatacount;i++){
                                    Mytimetable t=new Mytimetable();
                                    //one 요일 길이 추출해 저장
                                    int day=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));//4byte 길이정보 int 변환
                                    pos+=4;//4byte 읽었으므로 pos를 4만큼 증가

                                    int starttimelength=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));//4byte 길이정보 int 변환
                                    pos+=4;//4byte 읽었으므로 pos를 4만큼 증가
                                    byte[] word = Arrays.copyOfRange(buf, pos, pos+starttimelength);//추출한길이만큼읽어 시작시간추출
                                    String onestarttime = new String(word);//추출 word를  string으로 변환하여 저장

                                    t.setClassstarttime(onestarttime);
                                    pos+=starttimelength;// 시작시간길이만큼 읽었으므로 pos를 시작시간 길이만큼 증가

                                    //one name 길이 추출해 저장
                                    int endtimelength=proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos+4));//4byte 길이정보 int 변환
                                    pos+=4;//4byte 읽었으므로 pos를 4만큼 증가
                                    word = Arrays.copyOfRange(buf, pos, pos+endtimelength);//추출한길이만큼읽어 종료시간추출
                                    String oneendtime = new String(word);//추출 word를  string으로 변환하여 저장

                                    t.setClassendtime(oneendtime);
                                    pos+=endtimelength;// 종료시간길이만큼 읽었으므로 pos를 종료시간 길이만큼 증가
                                    if(day==1){
                                        monlist.add(t);
                                    }
                                    else if(day==2){
                                        tuslist.add(t);
                                    }
                                    else if(day==3){
                                        wenlist.add(t);
                                    }
                                    else if(day==4){
                                        thrlist.add(t);
                                    }
                                    else if(day==5){
                                        frylist.add(t);
                                    }
                                }
                                Collections.sort(monlist);
                                Collections.sort(tuslist);
                                Collections.sort(wenlist);
                                Collections.sort(thrlist);
                                Collections.sort(frylist);
                                //list foreach반복문돌면서 이쁘게 출력
                                System.out.println("월");
                                for (Mytimetable t:monlist) {t.printmyinfo();}
                                System.out.println("화");
                                for (Mytimetable t:tuslist) {t.printmyinfo();}
                                System.out.println("수");
                                for (Mytimetable t:wenlist) {t.printmyinfo();}
                                System.out.println("목");
                                for (Mytimetable t:thrlist) {t.printmyinfo();}
                                System.out.println("금");
                                for (Mytimetable t:frylist) {t.printmyinfo();}
                                proto.setPacket(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                        }
                        break;
                        //-------------------------------------
                    case Rprotocol.REGIST_CANSEL_RESULT://수강취소결과받고 메뉴출력요청보냄
                        System.out.println("수강취소성공");
                        proto.setPacket(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                        bos.write(proto.getPacket());
                        bos.flush();
                        break;
                        //--------------------------------------
                    case Rprotocol.STU_INFO_UPDATE_REQ:
                        switch (packetCode){
                            case Rprotocol.PWD_UPDATE_IN_REQ_CODE:
                                System.out.println("변경할 비밀번호 입력");
                                pos=0;
                                String newpwd=sc.next();
                                byte[] newpwdbyte=newpwd.getBytes();
                                payload=new byte[70];//선택과목 코드길이,코드저장할배열
                                int newpwdbytelenlen=newpwdbyte.length;
                                System.arraycopy(proto.intto4byte(newpwdbytelenlen),0,payload,pos,proto.intto4byte(newpwdbytelenlen).length);
                                pos+=4;
                                System.arraycopy(newpwd.getBytes(),0,payload,pos,newpwd.getBytes().length);

                                proto.setPacket(Rprotocol.PT_UNDEFINED, Rprotocol.STU_INFO_UPDATE_ANS, Rprotocol.PT_UNDEFINED, payload);

                                bos.write(proto.getPacket());
                                bos.flush();
                                break;
                        }
                        break;
                        //-----------------------------------------
                    case Rprotocol.STU_INFO_UPDATE_RESULT:
                        System.out.println("비밀번호 변경 완료");
                        proto.setPacket(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                        bos.write(proto.getPacket());
                        bos.flush();
                        break;
                        //------------------------------------------
                    case Rprotocol.SUBJECT_PLAN_RESULT:
                        System.out.println("선택한 교과목 강의계획서 정보");
                        pos=3;
                        int planlength=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));//4byte 길이정보 int 변환
                        pos+=4;//4byte 읽었으므로 pos를 4만큼 증가

                        byte[] word = Arrays.copyOfRange(buf, pos, pos+planlength);//추출한길이만큼읽어 코드추출
                        String onecode = new String(word);//추출 word를  string으로 변환하여 저장

                        System.out.println("강의계획서:"+onecode);
                        proto.setPacket(Rprotocol.MENU_REQ, Rprotocol.PT_UNDEFINED, Rprotocol.PT_UNDEFINED);
                        bos.write(proto.getPacket());
                        bos.flush();
                        break;
                }
            }
        }
        s.close();
    }

}



