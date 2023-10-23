package com.nhnacademy.aiot;

import com.nhnacademy.aiot.exception.InvalidRequestException;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class ServiceHandler extends Thread {

    private Socket socket;

    public ServiceHandler(Socket socket) {
        this.socket = socket;
    }

    private Request receiveRequest(BufferedReader socketIn) throws IOException {
        String[] splited = socketIn.readLine().split(" ");
        Request request = new Request(splited[0], splited[1], splited[2]);

        String line;
        while ((line = socketIn.readLine()) != null) {
            if (line.length() == 0) {
                break;
            }

            int colonIndex = line.indexOf(':');
            if (colonIndex < 0) {
                throw new InvalidRequestException();
            }

            request.addHeader(line.substring(0, colonIndex).trim(), line.substring(colonIndex + 1).trim());
        }

        if (request.getMethod().equals("POST") && request.isExistHeader("Content-Length")) {
            StringBuilder sb = new StringBuilder();
            while ((line = socketIn.readLine()) != null) {
                sb.append(line).append("\r\n");
                if (line.length() == 0 && sb.length() >= Integer.parseInt(request.getHeader("Content-Length"))) {
                    break;
                }
            }
            request.addBody(sb.toString());
        }
        return request;
    }

    private Response getFileList() {
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.addHeader("Content-Type", "text/html; charset=utf-8");
        response.addBody(makeListHtmlBody());
        return response;
    }

    private String makeListHtmlBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html>");
        sb.append("<head><meta charset=\"UTF\"><title>FileList</title></head>");
        sb.append("<body><ul>");

        Stream.of(Objects.requireNonNull(new File(SimpleHttpd.DOCUMENT_ROOT).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .forEach(name -> sb.append(String.format("<li><a href=\"/%s\">%s</a></li>", name, name)));

        sb.append("</ul></body></html>");
        return sb.toString();
    }

    private Response getFileContent(File file) {
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.addHeader("Content-Type", "text/plain; charset=utf-8");

        StringBuilder sb = new StringBuilder();
        try (FileReader reader = new FileReader(file)) {
            int integer;
            while ((integer = reader.read()) != -1) {
                sb.append((char) integer);
            }
        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }

        response.addBody(sb.toString());
        return response;
    }

    private void sendResponse(BufferedWriter socketOut, Response response) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%s %s", response.getVersion(), response.getStatus().toString())).append("\r\n");

        if (response.isExistsBody()) {
            response.addHeader("Content-Length", String.valueOf(response.getBody().length()));
        }
        response.getHeaders().forEach((key, value) -> sb.append(String.format("%s: %s", key, value)).append("\r\n"));

        if (response.isExistsBody()) {
            sb.append("\r\n").append(response.getBody()).append("\r\n");
        }

        socketOut.write(sb.toString());
        socketOut.flush();
    }

    private boolean isExistFile(String filename) {
        return Arrays.stream(Objects.requireNonNull(new File(SimpleHttpd.DOCUMENT_ROOT).listFiles()))
                .filter(file -> !file.isDirectory())
                .anyMatch(file -> filename.equals(file.getName()));
    }

    @Override
    public void run() {
        try (BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter socketOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            Request request = receiveRequest(socketIn);

            Response response = new Response();
            if (request.getMethod().equals("GET")) {
                if (request.getPath().equals("/")) {
                    response = getFileList();
                } else {
                    if (SimpleHttpd.DOCUMENT_ROOT.contains(request.getPath())) {
                        response.setStatus(HttpStatus.FORBIDDEN);
                    } else if (isExistFile(request.getPath().replaceFirst("/", ""))) {
                        File file = new File(SimpleHttpd.DOCUMENT_ROOT + request.getPath());
                        if (file.canRead()) {
                            response = getFileContent(file);
                        } else {
                            response.setStatus(HttpStatus.FORBIDDEN);
                        }
                    } else {
                        response.setStatus(HttpStatus.NOT_FOUND);
                    }
                }
            } else if (request.getMethod().equals("POST")) {
                if (request.isExistHeader("Content-Type")
                        && request.getHeader("Content-Type").contains("multipart/form-data")) {
                    fileUpload(request, response);
                } else {
                    response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                }
            }
            sendResponse(socketOut, response);

        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void fileUpload(Request request, Response response) throws IOException {
        File file = new File(SimpleHttpd.DOCUMENT_ROOT + request.getPath() + "/upload.txt");

        String[] splited = request.getBody().split("--" + request.getHeader("Content-Type").split("boundary=")[1]);
        for (int i = 1; i < splited.length; i++) {
            if (!splited[i].contains("Content-Disposition")) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            for (String s : splited[i].split("\r\n\r\n")) {
                if (s.contains("filename")) {
                    String filename = s.substring(s.lastIndexOf("filename=") + 9);
                    file = new File(SimpleHttpd.DOCUMENT_ROOT + request.getPath() + "/" + filename);
                } else {
                    sb.append(s);
                }
            }

            if (!file.createNewFile()) {
                response.setStatus(HttpStatus.CONFLICT);
                return;
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(sb.toString());
                writer.flush();
            }
        }

        response.setStatus(HttpStatus.OK);
    }

    /*
    POST / HTTP/1.1
    Host: httpbin.org
    Content-Length: 124
    Content-Type: multipart/form-data; boundary=----ABCDEF

    ------ABCDEF
    Content-Disposition: form-data; name=upload; filename=test.txt

    test file
    hello world!
    안녕!!
    ------ABCDEF--
     */
}
