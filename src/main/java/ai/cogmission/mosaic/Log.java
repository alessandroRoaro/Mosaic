package ai.cogmission.mosaic;

public class Log {

	public enum LogLevel {

		DEBUG(0),
		VERBOSE(1),
		INFO(2),
		ERROR(3);

		private int level;

		private LogLevel (int level) {
			this.level = level;
		}


		public int getLevel() {
			return level;
		}
	}

	
	private static int logLevel = LogLevel.DEBUG.getLevel();

	public static void d (String msg) {
		if (logLevel == LogLevel.DEBUG.getLevel()) {
			System.out.println(msg);
		}
	}
	
	
	public static void v (String msg) {
		if (logLevel <= LogLevel.VERBOSE.getLevel()) {
			System.out.println(msg);
		}
	}
	
	
	public static void i (String msg) {
		if (logLevel <= LogLevel.INFO.getLevel()) {
			System.out.println(msg);
		}
	}


	public static void e (String msg) {
		if (logLevel <= LogLevel.ERROR.getLevel()) {
			System.err.println(msg);
		}
	}
}
