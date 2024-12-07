package org.example.mvc.view;

import org.example.global.Protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class AdminView {
    private final Scanner scanner;
    private final DataInputStream in;
    private final DataOutputStream out;

    public AdminView(Socket socket, DataInputStream in, DataOutputStream out) {
        this.scanner = new Scanner(System.in);
        this.in = in;
        this.out = out;
    }

    public void showMenu() {
        while (true) {
            try {
                System.out.println("\n=== 기숙사 관리자 시스템 ===");
                System.out.println("1. 일정 및 비용 등록");
                System.out.println("2. 신청자 조회");
                System.out.println("3. 입사자 선발");
                System.out.println("4. 호실 배정");
                System.out.println("5. 비용 납부 확인");
                System.out.println("6. 진단서 제출 확인");
                System.out.println("0. 로그아웃");
                System.out.print("메뉴 선택: ");

                int choice = scanner.nextInt();

                switch (choice) {
                    case 1 -> handleScheduleAndFee();
                    case 2 -> handleApplicationList();
                    case 3 -> handleStudentSelection();
                    case 4 -> handleRoomAssignment();
                    case 5 -> handlePaymentCheck();
                    case 6 -> handleDocumentCheck();
                    case 0 -> {
                        System.out.println("로그아웃 합니다.");
                        return;
                    }
                    default -> {
                        System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
                        System.out.print("계속하려면 엔터를 누르세요...");
                        scanner.nextLine();
                        scanner.nextLine();
                    }
                }
            } catch (InputMismatchException e) {
                System.out.println("올바른 숫자를 입력해주세요.");
                scanner.nextLine();
                System.out.print("계속하려면 엔터를 누르세요...");
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("오류 발생: " + e.getMessage());
                e.printStackTrace();
                System.out.print("계속하려면 엔터를 누르세요...");
                scanner.nextLine();
                scanner.nextLine();
            }
        }
    }

    private void handleApplicationList() throws IOException {
        System.out.println("\n=== 신청자 조회 ===");
        Protocol protocol = new Protocol(Protocol.TYPE_APPLICATION, Protocol.CODE_APPLICATION_LIST);
        sendProtocol(protocol);
    }

    private void handleScheduleAndFee() throws IOException {
        while (true) {
            System.out.println("\n=== 일정 및 비용 등록 ===");
            System.out.println("1. 일정 등록");
            System.out.println("2. 비용 등록");
            System.out.println("0. 이전 메뉴로");
            System.out.print("선택: ");

            try {
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1 -> registerSchedule();
                    case 2 -> registerFee();
                    case 0 -> {
                        return;
                    }
                    default -> {
                        System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
                        System.out.print("계속하려면 엔터를 누르세요...");
                        scanner.nextLine();
                        scanner.nextLine();
                    }
                }
            } catch (InputMismatchException e) {
                System.out.println("올바른 숫자를 입력해주세요.");
                scanner.nextLine();
                System.out.print("계속하려면 엔터를 누르세요...");
                scanner.nextLine();
            }
        }
    }

    private void registerSchedule() throws IOException {
        scanner.nextLine(); // 버퍼 비우기
        System.out.println("\n=== 일정 등록 ===");
        System.out.print("일정 이름을 입력해주세요: ");
        String eventName = scanner.nextLine();
        System.out.print("신청 시작일(YYYY-MM-DD HH:mm) : ");
        String startDate = scanner.nextLine();
        System.out.print("신청 종료일(YYYY-MM-DD HH:mm): ");
        String endDate = scanner.nextLine();

        String scheduleData = eventName + "," + startDate + "," + endDate;
        Protocol protocol = new Protocol(Protocol.TYPE_SCHEDULE, Protocol.CODE_SCHEDULE_REG);
        protocol.setData(scheduleData);
        sendProtocol(protocol);
    }

    private void registerFee() throws IOException {
        scanner.nextLine(); // 버퍼 비우기
        System.out.println("\n=== 비용 등록 ===");
        System.out.print("기숙사비(원): ");
        String fee = scanner.nextLine();

        Protocol protocol = new Protocol(Protocol.TYPE_SCHEDULE, Protocol.CODE_SCHEDULE_FEE_REG);
        protocol.setData(fee);
        sendProtocol(protocol);
    }

    private void handleStudentSelection() throws IOException {
        System.out.println("\n=== 입사자 선발 ===");
        Protocol protocol = new Protocol(Protocol.TYPE_ROOM, Protocol.CODE_ROOM_SELECT);
        sendProtocol(protocol);
    }

    private void handleRoomAssignment() throws IOException {
        System.out.println("\n=== 호실 배정 ===");
        Protocol protocol = new Protocol(Protocol.TYPE_ROOM, Protocol.CODE_ROOM_ASSIGN);
        sendProtocol(protocol);
    }

    private void handlePaymentCheck() throws IOException {
        while (true) {
            System.out.println("\n=== 비용 납부 확인 ===");
            System.out.println("1. 납부자 명단");
            System.out.println("2. 미납자 명단");
            System.out.println("0. 이전 메뉴로");
            System.out.print("선택: ");

            try {
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1 -> {
                        Protocol protocol = new Protocol(Protocol.TYPE_PAYMENT, Protocol.CODE_PAYMENT_PAID_LIST);
                        sendProtocol(protocol);
                        return;
                    }
                    case 2 -> {
                        Protocol protocol = new Protocol(Protocol.TYPE_PAYMENT, Protocol.CODE_PAYMENT_UNPAID_LIST);
                        sendProtocol(protocol);
                        return;
                    }
                    case 0 -> {
                        return;
                    }
                    default -> {
                        System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
                        System.out.print("계속하려면 엔터를 누르세요...");
                        scanner.nextLine();
                        scanner.nextLine();
                    }
                }
            } catch (InputMismatchException e) {
                System.out.println("올바른 숫자를 입력해주세요.");
                scanner.nextLine();
                System.out.print("계속하려면 엔터를 누르세요...");
                scanner.nextLine();
            }
        }
    }

    private void handleDocumentCheck() throws IOException {
        while (true) {
            System.out.println("\n=== 진단서 제출 확인 ===");
            System.out.println("1. 제출자 명단");
            System.out.println("2. 미제출자 명단");
            System.out.println("0. 이전 메뉴로");
            System.out.print("선택: ");

            try {
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1 -> {
                        Protocol protocol = new Protocol(Protocol.TYPE_DOCUMENT, Protocol.CODE_DOCUMENT_STATUS);
                        sendProtocol(protocol);
                        return;
                    }
                    case 2 -> {
                        Protocol protocol = new Protocol(Protocol.TYPE_DOCUMENT, Protocol.CODE_DOCUMENT_STATUS);
                        protocol.setData("unsubmitted"); // 미제출자 구분을 위한 데이터
                        sendProtocol(protocol);
                        return;
                    }
                    case 0 -> {
                        return;
                    }
                    default -> {
                        System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
                        System.out.print("계속하려면 엔터를 누르세요...");
                        scanner.nextLine();
                        scanner.nextLine();
                    }
                }
            } catch (InputMismatchException e) {
                System.out.println("올바른 숫자를 입력해주세요.");
                scanner.nextLine();
                System.out.print("계속하려면 엔터를 누르세요...");
                scanner.nextLine();
            }
        }
    }

    private void sendProtocol(Protocol protocol) throws IOException {
        byte[] packetData = protocol.getPacket();

        for (byte packetDatum : packetData) {
            out.writeByte(packetDatum);
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
            System.out.println("서버 응답: " + response.getDataAsString());
        }
    }
}