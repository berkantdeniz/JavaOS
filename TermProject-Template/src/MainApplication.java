public class MainApplication {

	public static void main(String[] args) throws InterruptedException {
		//Assembler assembler = new Assembler();
		OS os = new OS(6000);
		//os.loadProcess("assemblyInput.asm", assembler);

		os.start();


	}
}
