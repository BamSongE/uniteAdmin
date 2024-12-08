package org.example;

import org.example.mvc.view.FirstView;
import org.example.mvc.view.LoginView;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Socket clientSocket = new Socket("172.30.67.203", 8888)) {
            //안서호 pc ip 주소: 172.30.86.32, port: 8888
            //윤동근 pc ip 주소: 172.30.67.203, port: 8888
            Scanner sc = new Scanner(System.in);

            System.out.println("서버 연결 성공");

            // 버퍼 스트림으로 감싸서 성능 향상
            BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream());
            BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());

            // Data 스트림 초기화
            DataOutputStream out = new DataOutputStream(bos);
            DataInputStream in = new DataInputStream(bis);

            FirstView firstView = new FirstView();

            // LoginView 생성 및 의존성 주입
            LoginView loginView = new LoginView(in, out);

            System.out.println("안녕하세요. 기숙사 관리 시스템입니다.");
            firstView.view();
            int choice = sc.nextInt();
            while (choice != 3){
                if(choice == 1) {
                    loginView.printSignIn();
                }
//            else if(sc.nextInt() == 2) {
//                  회원가입 기능 추가 예정
//            }
                else {
                    System.out.println("정상적인 입력이 아닙니다.");
                }
                firstView.view();
                choice = sc.nextInt();
            }

            System.out.println("연결을 종료합니다.");
        } catch (UnknownHostException e) {
            System.err.println("호스트를 찾을 수 없습니다: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("서버 연결 실패: " + e.getMessage());
        }
    }
}