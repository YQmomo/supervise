package gov.df.fap.util.Properties;

public class WebContextParam {

  private static String startup = "Fasp_2.0";

  private static boolean develop = false;
  
  private static boolean enableAutoTask = true;

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public static String getStartup() {
    return startup;
  }

  public static void setStartup(String startup) {
    WebContextParam.startup = startup;
  }

  public static boolean isDevelop() {
    return develop;
  }

  public static void setDevelop(boolean develop) {
    WebContextParam.develop = develop;
  }

public static boolean isEnableAutoTask() {
	return enableAutoTask;
}

public static void setEnableAutoTask(boolean enableAutoTask) {
	WebContextParam.enableAutoTask = enableAutoTask;
}

}
