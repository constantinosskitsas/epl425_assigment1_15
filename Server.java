import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
/*
 * Wait user request,accepts and creates random payload and send back
 * with a welcome message.Calculates throughput.
 */

public class Server {
	private ServerSocket server;
	
	private static Integer requestsServed = 0;
	public static final long ONESECOND=1000000000;
	private static int port = 0;
	private static Integer maxRequests = 0;
	private static long start;
	private long interval = ONESECOND;
	/**
	 * crates a randomPayload to send back to the user
	 * @return
	 */
	public String randomPayload() {
		int size = (int) (1700000 * Math.random() + 300000);
		StringBuilder stringBuilder = new StringBuilder(size);
		for (int i = 0; i < size; i++) {
			stringBuilder.append('a');
		}
		return stringBuilder.toString();
	}

	
	public class UserThread extends Thread {
		DataInputStream reader;
		DataOutputStream writer;
		Socket uSocket;
/**Constructor
 * 
 * @param socketToUse
 */
		public UserThread(Socket socketToUse) {

			try {
				uSocket = socketToUse;
				reader = new DataInputStream( uSocket.getInputStream());
					requestsServed++;
					maxRequests--;	
				writer = new DataOutputStream(uSocket.getOutputStream());
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	/**takes from string and finds id number of the user request
	 * 
	 * @param from
	 * @return id
	 */
		public String findId(String from) {
			String aString = "";
			String[] sth=from.split(" ");
			return sth[2];
		}

		@Override
		/**
		 * reads user message and responds also calculates throughput
		 */
		public void run() {
			String messageReceived;
			String messageToSend="";
			boolean flag=false;
			try {
				while (maxRequests>0&&(messageReceived = reader.readUTF()) != null) {
					System.out.println(messageReceived);
					messageToSend="Welcome + Id:"+ findId(messageReceived);
					writer.writeUTF(messageToSend);
						
						maxRequests--;
						requestsServed++;
						if (maxRequests <= 0) {
							if(!flag)
							System.out.println(requestsServed);		
							flag=true;
							writer.close();
							server.close();
							reader.close();
						}
					if(!flag){	
					if (interval < System.nanoTime() - start) {
						System.out.println(requestsServed);
						start = System.nanoTime();
					}
					
					}
					writer.flush();
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
/**creates server socket and waits request from users
 * 
 */
	public void startServer() {
		boolean flag=false;
		try {
			 server = new ServerSocket(port);
			while (true) {
				Socket mySocket = server.accept();
				if(!flag)
					start = System.nanoTime();
				UserThread aThread=new UserThread(mySocket);
				aThread.start();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	

	public static void main(String[] args) {
		port=Integer.parseInt(args[0]);
		maxRequests=Integer.parseInt(args[1]);
		Server rela = new Server();
		rela.startServer();
	}

}
