package maxcu;
import py4j.GatewayServer;

public class PythonTest {

	public static void main(String[] args) {
		GatewayServer gs = new GatewayServer(new Facade());
		gs.start();
		System.out.println("GESTARTET");
	}

}
