package org.example.mvc.view;

import java.util.Scanner;

public class FirstView {
    private final Scanner scanner;
    //private final LoginView loginView;

    public FirstView() {
        this.scanner = new Scanner(System.in);
        //this.loginView = loginView;
    }


    public void view() {
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
