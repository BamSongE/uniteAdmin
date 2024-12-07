package org.example.mvc.view;

import lombok.Getter;
import org.example.global.Protocol;
import org.example.mvc.packet.LoginPacket;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class LoginView {
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Scanner scanner;

    public LoginView(Socket socket, DataInputStream in, DataOutputStream out) {
        this.in = in;
        this.out = out;
        this.scanner = new Scanner(System.in);
    }

    public void printSignIn() {
        try {
            System.out.println("=== 로그인 ===");
            System.out.print("아이디: ");
            String id = scanner.next();
            System.out.print("비밀번호: ");
            String pwd = scanner.next();

            // ID 검증
            LoginPacket idPacket = new LoginPacket(id, "id");
            System.out.println("전송할 ID 패킷: " + idPacket);

            // 패킷 전송
            byte[] packetData = idPacket.getPacket();
            for (int i = 0; i < packetData.length; i++) {
                out.writeByte(packetData[i]);
            }
            out.flush();

            // 서버 응답 읽기
            byte type = in.readByte();
            byte code = in.readByte();
            short length = in.readShort();

            byte[] data = null;
            if (length > 0) {
                data = new byte[length];
                in.readFully(data);
            }

            Protocol response = new Protocol(type, code);
            if (data != null) {
                response.setData(data);
            }

            // ID 검증 실패시 종료
            if (response.getCode() != Protocol.CODE_FAIL) {
                System.out.println("ID 검증 실패");
                return;
            }

            // 비밀번호 검증
            LoginPacket pwdPacket = new LoginPacket(id + "," + pwd, "pwd");
            System.out.println("전송할 Password 패킷: " + pwdPacket);

            packetData = pwdPacket.getPacket();
            for (int i = 0; i < packetData.length; i++) {
                out.writeByte(packetData[i]);
            }
            out.flush();

            // 서버 응답 읽기
            type = in.readByte();
            code = in.readByte();
            length = in.readShort();

            data = null;
            if (length > 0) {
                data = new byte[length];
                in.readFully(data);
            }

            response = new Protocol(type, code);
            if (data != null) {
                response.setData(data);
            }

            if (response.getType() == Protocol.TYPE_RESPONSE &&
                    response.getCode() == Protocol.CODE_SUCCESS) {
                System.out.println("로그인 성공!");
            } else {
                System.out.println("로그인 실패: " + response.getDataAsString());
            }

        } catch (Exception e) {
            System.out.println("로그인 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}