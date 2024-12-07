package org.example.mvc.view;

import java.util.Scanner;

public class FirstView {
    private final Scanner scanner;
    private final LoginView loginView;

    public FirstView(LoginView loginView) {
        this.scanner = new Scanner(System.in);
        this.loginView = loginView;
    }

    public void View() {
        System.out.println("안녕하세요. 기숙사 관리 시스템입니다.");
        System.out.println("수행할 일을 아래 번호로 입력해주세요.");
        System.out.println("1. 로그인\t2. 회원가입\t3.종료");
    }
    public void signInView() {
        loginView.printSignIn();
    }
//    public void signUpView() {
//        loginView.printSignUp();
//    }
}
