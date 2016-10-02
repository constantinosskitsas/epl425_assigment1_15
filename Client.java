
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
/**
 * simulate the concurrent users while the request
 * information form the server.Measures the latency
 * of request
 * 
 * @author constantinos skitsas cskits01
 *
 */
public class Client {

	static Integer nextid = 1;
	static ArrayList<Long> allLatency = new ArrayList<Long>();
	public class SingleUser extends Thread {
		private String ip;
		private int port;
		private int requestsToDo = 300;
		private Socket userSocket;
		private DataOutputStream writer;
		private DataInputStream reader;
		
		int myId = 0;
		
		public SingleUser(String ip,int port) {
			this.ip=ip;
			this.port=port;
			synchronized (nextid) {
				myId = nextid++;
			}
			establishConnection();
		}

		@Override
		/** Wait server respond.After server response calculates latency
		 * end when all request served.
		*/
		public void run() {
			String messageRead="";
			Boolean finishFlag=false;
			long sendRequestTime = System.nanoTime();
			long receiveAnswerTime = System.nanoTime();
			long latency = 0;

			try {
				while (!finishFlag&&(messageRead = reader.readUTF()) != null) {
					receiveAnswerTime = System.nanoTime();
					requestsToDo--;
					if (requestsToDo == 0) {
						reader.close();
						writer.close();
						this.userSocket.close();
						latency = receiveAnswerTime - sendRequestTime;
						allLatency.add(latency);
						finishFlag=true;
					} else {
						latency = receiveAnswerTime - sendRequestTime;
						allLatency.add(latency);
						writer.writeUTF("HELLO " + InetAddress.getLocalHost().getHostAddress() 
								+ " " + port + " " + myId);
						sendRequestTime = System.nanoTime();
						writer.flush();
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			Long Average=(long)0;
			for (Long long1 : allLatency) {
				Average=Average+long1/(long)Math.pow(10, 6);
			}
			Average=Average/allLatency.size();
			System.out.println(Average);

		}
	
	/**
	 * Establish Connection with the server
	 */
		private void establishConnection() {
			try {
				userSocket = new Socket(ip, port);
				writer = new DataOutputStream(userSocket.getOutputStream());
				reader = new DataInputStream(userSocket.getInputStream());
				writer.writeUTF("HELLO " + InetAddress.getLocalHost().getHostAddress() 
						+ " " + port + " " + myId);
				writer.flush();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			Client.SingleUser mUser = new Client().new SingleUser(args[0],Integer.parseInt(args[1]));
			mUser.start();
		}

	}

}
