package org.example;

import org.example.mvc.view.FirstView;
import org.example.mvc.view.LoginView;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) {
        try (Socket clientSocket = new Socket("172.30.67.203", 8888)) {
            System.out.println("서버 연결 성공");

            // 버퍼 스트림으로 감싸서 성능 향상
            BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream());
            BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());

            // Data 스트림 초기화
            DataOutputStream out = new DataOutputStream(bos);
            DataInputStream in = new DataInputStream(bis);

            // LoginView 생성 및 의존성 주입
            LoginView loginView = new LoginView(clientSocket, in, out);

            // FirstView에 LoginView 전달
            FirstView firstView = new FirstView(loginView);
            firstView.firstView();

        } catch (UnknownHostException e) {
            System.err.println("호스트를 찾을 수 없습니다: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("서버 연결 실패: " + e.getMessage());
        }
    }
}