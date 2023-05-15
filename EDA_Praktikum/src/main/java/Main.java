import gui.ZFTGui;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    private static final String ASSETS = "./assets";
    private static final String VPR = ASSETS + "/vpr.exe";

    public static void main(String[] args) {
        new ZFTGui();
        /*
        try {
            String[] cmd = {VPR, "C:/Users/janik/OneDrive/Desktop/technische_Informatik/Semester " + "6/EDA/Praktikum" +
                    "/net/alu4.net", "C:/Users/janik/OneDrive/Desktop/technische_Informatik/Semester " + "6/EDA" +
                    "/Praktikum/net/4lut_sanitized.arch", "C:/Users/janik/OneDrive/Desktop/technische_Informatik" +
                    "/Semester 6/EDA/Praktikum/net/alu4temp.place", "C:/Users/janik/OneDrive/Desktop" +
                    "/technische_Informatik/Semester 6/EDA/Praktikum/net/alu4temp.route", "-place_only"};
            Process p = Runtime.getRuntime().exec(cmd);
            System.out.println("Do Exe");

            System.out.println("finished");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        parser.NetlistParser parser = new parser.NetlistParser();
        List<CircuitElement> netlist = parser.parse("C:/Users/janik/OneDrive/Desktop/technische_Informatik/Semester
        6/EDA/Praktikum/net/alu4.net");
        algorithm.ZFTAlgorithm algorithm = new algorithm.ZFTAlgorithm(netlist, 10);
        algorithm.run();
        writer.PlacementWriter writer = new writer.PlacementWriter();
        writer.write("C:/Users/janik/OneDrive/Desktop/technische_Informatik/Semester 6/EDA/Praktikum/net/alu4.place",
         "net/alu4.net", algorithm.getPlacements());
        System.out.println("Fertig");*/
    }
}
