package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
				connection.getPort());

		// 요구사항1 - index.html 응답하기
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = br.readLine();
			log.debug("line : {}", line);
			if (line == null) {
				return;
			}
			String url = HttpRequestUtils.getUrl(line);
			log.debug("Url : {}", url);

			// 요구사항2 - GET 방식으로 회원가입하기
			if (url.startsWith("/user/create")) {
				String queryString = null;

				if (line.startsWith("GET")) {
					int index = url.indexOf("?");
					queryString = url.substring(index + 1);
				} else {
					// 요구사항3 - POST 방식으로 회원가입하기
					while (!"".equals(line)) {
						log.debug("line : {}", line);
						line = br.readLine();
					}
					queryString = br.readLine();
					log.debug("queryString : {}",queryString);
				}

				Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);
				User user = new User(params.get("userId"), params.get("password"), params.get("name"),
						params.get("email"));
				log.debug("User : {}", user);

				// 회원가입 후 index.html 화면으로 이동
				url = "/index.html";
			}

			log.debug("webapp url : {}", url);

			byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

			DataOutputStream dos = new DataOutputStream(out);
			response200Header(dos, body.length);
			responseBody(dos, body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
