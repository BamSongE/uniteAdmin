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

    public AdminView(DataInputStream in, DataOutputStream out) {
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
                System.out.println("6. 결핵진단서 제출 확인");
                System.out.println("7. 퇴사 신청 조회 및 환불");
                System.out.println("0. 로그아웃");
                System.out.print("메뉴 선택: ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> handleScheduleAndFee();
                    case 2 -> handleApplicationList();
                    case 3 -> handleStudentSelection();
                    case 4 -> handleRoomAssignment();
                    case 5 -> handlePaymentCheck();
                    case 6 -> handleDocumentCheck();
                    case 7 -> handleWithDrawalRequest();
                    case 0 -> {
                        System.out.println("로그아웃 합니다.");
                        return;
                    }
                    default -> {
                        System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
                        System.out.print("계속하려면 엔터를 누르세요...");
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
        System.out.print("조회할 상태를 입력하세요 ('대기', '선발', '승인', '거부'): ");
        String status = scanner.nextLine();
        System.out.print("기숙사 선호도를 입력하세요 (1지망이면 '1', 2지망이면 '2'): ");
        int preference = scanner.nextInt();

        // 요청 데이터 생성
        String requestData = String.format("%s,%s", status, preference);
        Protocol protocol = new Protocol(Protocol.TYPE_APPLICATION, Protocol.CODE_APPLICATION_LIST);
        protocol.setData(requestData.getBytes(StandardCharsets.UTF_8));
        sendProtocol(protocol);

        protocol = readProtocol();

        // 응답 데이터 활용
        if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_SUCCESS) {
            if (protocol.getData() != null) {
                String applicantList = new String(protocol.getData(), StandardCharsets.UTF_8); // UTF-8로 변환
                System.out.println("\n신청자 목록:");
                System.out.println(applicantList); // 출력 형식: "학번,이름,상태,납부상태,신청일"
            } else {
                System.out.println("신청자 목록이 비어 있습니다.");
            }
        } else if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_FAIL) {
            String errorMessage = protocol.getData() != null ? new String(protocol.getData(), StandardCharsets.UTF_8) : "알 수 없는 오류가 발생했습니다.";
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
                    }
                }

                printWannaGoNext();

            } catch (InputMismatchException e) {
                System.out.println("올바른 숫자를 입력해주세요.");
                printWannaGoNext();
            }
        }
    }

    private void registerSchedule() throws IOException {
        scanner.nextLine(); // 버퍼 비우기
        System.out.println("\n=== 일정 등록 ===");
        System.out.print("일정 이름을 입력해주세요: ");
        String eventName = scanner.nextLine();
        System.out.print("신청 시작일(YYYY-MM-DD 또는 YYYY-MM-DD HH:mm): ");
        String startDate = scanner.nextLine();
        System.out.print("신청 종료일(YYYY-MM-DD 또는 YYYY-MM-DD HH:mm): ");
        String endDate = scanner.nextLine();

        // 요청 데이터 생성
        String scheduleData = eventName + "," + startDate + "," + endDate;
        Protocol protocol = new Protocol(Protocol.TYPE_SCHEDULE, Protocol.CODE_SCHEDULE_REG);
        protocol.setData(scheduleData.getBytes(StandardCharsets.UTF_8));

        // 서버로 요청 전송
        sendProtocol(protocol);

        // 서버 응답 처리
        protocol = readProtocol();

        // 응답 데이터 활용
        if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_SUCCESS) {
            String successMessage = protocol.getData() != null
                    ? new String(protocol.getData(), StandardCharsets.UTF_8)
                    : "응답 데이터 없음";
            System.out.println("\n일정 등록 결과:");
            System.out.println(successMessage);
        } else if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_FAIL) {
            String errorMessage = protocol.getData() != null
                    ? new String(protocol.getData(), StandardCharsets.UTF_8)
                    : "알 수 없는 오류가 발생했습니다.";
            System.out.println("일정 등록 중 오류 발생: " + errorMessage);
        } else {
            System.out.println("서버에서 잘못된 응답을 받았습니다.");
        }
    }

    private void registerFee() throws IOException {
        scanner.nextLine(); // 버퍼 비우기
        System.out.println("\n=== 비용 등록 ===");
        System.out.print("기숙사 이름: ");
        String dormName = scanner.nextLine();

        System.out.println("1. 2인실 숙박비");
        System.out.println("2. 4인실 숙박비");
        System.out.println("3. 주 5일 식사비");
        System.out.println("4. 주 7일 식사비");
        System.out.println("설정하고 싶은 비용의 숫자를 입력하세요:");
        int feeChoose = scanner.nextInt();

        // 비용 유형 매핑
        String feeType;
        switch (feeChoose) {
            case 1 -> feeType = "ROOM_2";
            case 2 -> feeType = "ROOM_4";
            case 3 -> feeType = "MEAL_5";
            case 4 -> feeType = "MEAL_7";
            default -> {
                System.err.println("잘못된 선택입니다. 비용 등록을 종료합니다.");
                return;
            }
        }

        System.out.print("기숙사비(원): ");
        int fee = scanner.nextInt();
        // 요청 데이터 생성 및 전송
        String feeData = dormName + "," + feeType + "," + fee;
        Protocol protocol = new Protocol(Protocol.TYPE_SCHEDULE, Protocol.CODE_SCHEDULE_FEE_REG);
        protocol.setData(feeData.getBytes(StandardCharsets.UTF_8));
        sendProtocol(protocol);

        // 서버 응답 처리
        protocol = readProtocol();

        // 응답 데이터 활용
        if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_SUCCESS) {
            String successMessage = protocol.getData() != null
                    ? new String(protocol.getData(), StandardCharsets.UTF_8)
                    : "응답 데이터 없음";
            System.out.println("\n비용 등록 결과:");
            System.out.println(successMessage);
        } else if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_FAIL) {
            String errorMessage = protocol.getData() != null
                    ? new String(protocol.getData(), StandardCharsets.UTF_8)
                    : "알 수 없는 오류가 발생했습니다.";
            System.out.println("비용 등록 중 오류 발생: " + errorMessage);
        } else {
            System.out.println("서버에서 잘못된 응답을 받았습니다.");
        }
    }


    private void handleStudentSelection() throws IOException {
        System.out.println("\n=== 입사자 선발 ===");

        // 서버로 입사자 선발 요청 보내기
        Protocol protocol = new Protocol(Protocol.TYPE_ROOM, Protocol.CODE_ROOM_SELECT);
        sendProtocol(protocol);

        //서버 응답 처리
        protocol = readProtocol();

        // 응답 데이터 활용
        if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_SUCCESS) {
            String successMessage;
            if (protocol.getData() != null) {
                // 데이터가 있을 경우 UTF-8로 변환
                successMessage = new String(protocol.getData(), StandardCharsets.UTF_8);
            } else {
                successMessage = "응답 데이터 없음";
            }

            System.out.println("\n입사자 선발 결과:");
            System.out.println(successMessage);
        } else if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_FAIL) {
            String errorMessage = protocol.getData() != null ? new String(protocol.getData(), StandardCharsets.UTF_8) : "알 수 없는 오류가 발생했습니다.";
            System.out.println("입사자 선발 중 오류 발생: " + errorMessage);
        } else {
            System.out.println("서버에서 잘못된 응답을 받았습니다.");
        }

        printWannaGoNext();
    }

    /**
     * 호실 배정 요청
     * @throws IOException
     */
    private void handleRoomAssignment() throws IOException {
        System.out.println("\n=== 호실 배정 ===");

        // 서버로 호실 배정 요청 보내기
        Protocol protocol = new Protocol(Protocol.TYPE_ROOM, Protocol.CODE_ROOM_ASSIGN);
        sendProtocol(protocol);

        //서버 응답 처리
        protocol = readProtocol();

        // 응답 데이터 활용
        if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_SUCCESS) {
            String successMessage = protocol.getData() != null ? new String(protocol.getData(), StandardCharsets.UTF_8) : "호실 배정 결과 없음";
            System.out.println("\n호실 배정 결과:");
            System.out.println(successMessage);
        } else if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_FAIL) {
            String errorMessage = protocol.getData() != null ? new String(protocol.getData(), StandardCharsets.UTF_8) : "알 수 없는 오류가 발생했습니다.";
            System.out.println("호실 배정 중 오류 발생: " + errorMessage);
        } else {
            System.out.println("서버에서 잘못된 응답을 받았습니다.");
        }

        printWannaGoNext();
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

                Protocol protocol;
                switch (choice) {
                    case 1 -> {
                        protocol = new Protocol(Protocol.TYPE_PAYMENT, Protocol.CODE_PAYMENT_PAID_LIST);
                        sendProtocol(protocol);
                        handlePaymentResponse();
                        return; // 완료 후 이전 메뉴로 이동
                    }
                    case 2 -> {
                        protocol = new Protocol(Protocol.TYPE_PAYMENT, Protocol.CODE_PAYMENT_UNPAID_LIST);
                        sendProtocol(protocol);
                        handlePaymentResponse();
                        return; // 완료 후 이전 메뉴로 이동
                    }
                    case 0 -> {
                        return; // 이전 메뉴로 이동
                    }
                    default -> {
                        System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
                    }
                }

                printWannaGoNext();

            } catch (InputMismatchException e) {
                System.out.println("올바른 숫자를 입력해주세요.");
                scanner.nextLine(); // 잘못된 입력 처리
            }
        }
    }

    private void handlePaymentResponse() throws IOException {
        Protocol response = readProtocol();

        if (response.getType() == Protocol.TYPE_RESPONSE && response.getCode() == Protocol.CODE_SUCCESS) {
            // 응답 데이터를 UTF-8 문자열로 변환 후 출력
            String paymentList = response.getData() != null ? new String(response.getData(), StandardCharsets.UTF_8) : "결과 데이터 없음";
            System.out.println("\n결과:");
            System.out.println(paymentList);
        } else if (response.getType() == Protocol.TYPE_RESPONSE && response.getCode() == Protocol.CODE_FAIL) {
            // 실패 메시지 출력
            String errorMessage = response.getData() != null ? new String(response.getData(), StandardCharsets.UTF_8) : "알 수 없는 오류가 발생했습니다.";
            System.out.println("요청 처리 중 오류 발생: " + errorMessage);
        } else {
            System.out.println("서버에서 잘못된 응답을 받았습니다.");
        }
    }

    private void handleDocumentCheck() throws IOException {
            System.out.println("\n=== 결핵진단서 제출 여부 명단 ===");

        try {
            Protocol protocol = new Protocol(Protocol.TYPE_DOCUMENT, Protocol.CODE_DOCUMENT_STATUS);
            sendProtocol(protocol);

            protocol = readProtocol(); // 서버 응답 읽기

            // 응답 데이터 활용
            if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_SUCCESS) {
                String result = protocol.getData() != null
                        ? new String(protocol.getData(), StandardCharsets.UTF_8)
                        : "응답 데이터 없음";
                System.out.println("\n결과:");
                System.out.println(result);
            } else if (protocol.getType() == Protocol.TYPE_RESPONSE && protocol.getCode() == Protocol.CODE_FAIL) {
                String errorMessage = protocol.getData() != null
                        ? new String(protocol.getData(), StandardCharsets.UTF_8)
                        : "알 수 없는 오류가 발생했습니다.";
                System.out.println("오류 발생: " + errorMessage);
            } else {
                System.out.println("서버에서 잘못된 응답을 받았습니다.");
            }

            printWannaGoNext();

        } catch (InputMismatchException e) {
            System.out.println("올바른 숫자를 입력해주세요.");
            printWannaGoNext();
        }
    }

    private void handleWithDrawalRequest() throws IOException {
        while (true) {
            System.out.println("=== 퇴사 신청 조회 및 환불 ===");
            System.out.println("1. 퇴사 신청 조회");
            System.out.println("2. 환불");
            System.out.println("0. 이전 메뉴로");
            System.out.print("선택: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // 입력 버퍼 정리

                switch (choice) {
                    case 1 -> {
                        Protocol protocol = new Protocol(Protocol.TYPE_WITHDRAWAL, Protocol.CODE_WITHDRAWAL_LIST);
                        sendProtocol(protocol);

                        // 서버 응답 처리
                        Protocol response = readProtocol();
                        if (response.getCode() == Protocol.CODE_SUCCESS) {
                            System.out.println("퇴사 신청 목록:");
                            System.out.println(new String(response.getData(), StandardCharsets.UTF_8));
                        } else {
                            System.out.println("퇴사 신청 목록 조회 중 오류 발생: " + new String(response.getData(), StandardCharsets.UTF_8));
                        }
                    }
                    case 2 -> {
                        System.out.print("환불 처리할 학생 ID를 입력하세요: ");
                        Long withdrawalId = scanner.nextLong();
                        scanner.nextLine(); // 입력 버퍼 정리

                        Protocol protocol = new Protocol(Protocol.TYPE_WITHDRAWAL, Protocol.CODE_WITHDRAWAL_REFUND);
                        String refundData = String.valueOf(withdrawalId); // 환불 데이터 설정
                        protocol.setData(refundData.getBytes(StandardCharsets.UTF_8));
                        sendProtocol(protocol);

                        // 서버 응답 처리
                        Protocol response = readProtocol();
                        if (response.getCode() == Protocol.CODE_SUCCESS) {
                            System.out.println(new String(response.getData(), StandardCharsets.UTF_8));
                        } else {
                            System.out.println("환불 처리 중 오류 발생: " + new String(response.getData(), StandardCharsets.UTF_8));
                        }
                    }
                    case 0 -> {
                        return; // 메뉴 종료
                    }
                    default -> {
                        System.out.println("잘못된 선택입니다. 다시 선택해주세요.");
                    }
                }

                printWannaGoNext();

            } catch (InputMismatchException e) {
                System.out.println("올바른 숫자를 입력해주세요.");
                scanner.nextLine(); // 잘못된 입력 처리
                printWannaGoNext();
            }
        }
    }


    private void sendProtocol(Protocol protocol) throws IOException {
        byte[] packetData = protocol.getPacket();

        for (byte packetDatum : packetData) {
            out.writeByte(packetDatum);
        }
        out.flush();
    }

    private Protocol readProtocol() throws IOException {
        // 서버 응답 읽기
        byte type = in.readByte();
        byte code = in.readByte();
        short length = in.readShort();

        byte[] data = null;
        if (length > 0) {
            data = new byte[length];
            in.readFully(data);
        }

        // 응답 프로토콜 생성 및 반환
        Protocol response = new Protocol(type, code);
        if (data != null) {
            response.setData(data);
        }
        return response;
    }

    private void printWannaGoNext() {
        System.out.print("계속하려면 엔터를 누르세요...");
        scanner.nextLine();
    }
}