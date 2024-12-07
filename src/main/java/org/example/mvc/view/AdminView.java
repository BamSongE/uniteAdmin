package org.example.mvc.view;

import org.example.global.Protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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

        // 서버에 신청자 조회 요청 보내기
        scanner.nextLine(); // 입력 버퍼 비우기
        System.out.print("조회할 상태를 입력하세요 (예: '승인', '대기', '취소' 등): ");
        String status = scanner.nextLine();
        System.out.print("기숙사 선호도를 입력하세요 (예: '2인실', '4인실' 등): ");
        String preference = scanner.nextLine();

        // 요청 데이터 생성
        String requestData = String.format("%s,%s", status, preference);
        Protocol protocol = new Protocol(Protocol.TYPE_APPLICATION, Protocol.CODE_APPLICATION_LIST);
        protocol.setData(requestData.getBytes(StandardCharsets.UTF_8));
        sendProtocol(protocol);

        // 서버 응답 처리
        byte type = in.readByte();
        byte code = in.readByte();
        short length = in.readShort();

        byte[] data = null;
        if (length > 0) {
            data = new byte[length];
            in.readFully(data);
        }

        // 응답 데이터 활용
        if (type == Protocol.TYPE_RESPONSE && code == Protocol.CODE_SUCCESS) {
            if (data != null) {
                String applicantList = new String(data, StandardCharsets.UTF_8); // UTF-8로 변환
                System.out.println("\n신청자 목록:");
                System.out.println(applicantList); // 출력 형식: "학번,이름,상태,납부상태,신청일"
            } else {
                System.out.println("신청자 목록이 비어 있습니다.");
            }
        } else if (type == Protocol.TYPE_RESPONSE && code == Protocol.CODE_FAIL) {
            String errorMessage = data != null ? new String(data, StandardCharsets.UTF_8) : "알 수 없는 오류가 발생했습니다.";
            System.out.println("신청자 조회에 실패했습니다: " + errorMessage);
        } else {
            System.out.println("서버에서 잘못된 응답을 받았습니다.");
        }
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
        System.out.print("기숙사 이름: ");
        String dorName = scanner.nextLine();
        System.out.println("설정하고 싶은 비용의 숫자를 입력하세요");
        System.out.println("1. 2인실 숙박비");
        System.out.println("2. 4인실 숙박비");
        System.out.println("3. 주 5일 식사비");
        System.out.println("4. 주 7일 식사비");
        int feeChoose = scanner.nextInt();
        System.out.print("기숙사비(원): ");
        int fee = scanner.nextInt();

        String feeType;
        switch (feeChoose) {
            case 1:
                feeType = "ROOM_2";
                break;
            case 2:
                feeType = "ROOM_4";
                break;
            case 3:
                feeType = "MEAL_5";
                break;
            case 4:
                feeType = "MEAL_7";
                break;
            default:
                feeType = null;
                System.err.println("정상적인 입력이 아닙니다.");
                System.err.println("비용 등록을 종료합니다.");
                break;
        }

        if(feeType != null) {
            Protocol protocol = new Protocol(Protocol.TYPE_SCHEDULE, Protocol.CODE_SCHEDULE_FEE_REG);
            protocol.setData(dorName + "," + feeType + "," + fee);
            sendProtocol(protocol);
        }

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