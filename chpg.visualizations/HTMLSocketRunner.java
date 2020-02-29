package chpg.visualizations;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class HTMLSocketRunner implements Runnable {

	public Object sync;
	private int port;
	private String htmlContents;
	
	public HTMLSocketRunner(Object sync, int port, String htmlContents) {
		this.sync = sync;
		this.port = port;
		this.htmlContents = htmlContents;
	}

	@Override
	public void run() {
		// Create a ServerSocket at port for serving up htmlContents
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Set socket timeout so server.accept() does not endlessly block
		try {
			server.setSoTimeout(10);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// Listen for connections
		while (true) {
			try (Socket socket = server.accept()) {
				// When a connection is made, write htmlContents to the socket
				String httpResponse = "HTTP/1.1 200 OK\r\n"
						+ "Access-Control-Allow-Origin: *\r\n" + htmlContents;
				socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));				
				socket.getOutputStream().flush();
				socket.close();
				server.close();
				return;
			} catch (SocketTimeoutException e) {
				// Ignore socket timeouts
			} catch (IOException e) {
				// Display all other exceptions
				e.printStackTrace();
			}

			// Notify that the SocketServer has started listening
			synchronized (sync) {
				sync.notify();
			}
		}
	}

}
