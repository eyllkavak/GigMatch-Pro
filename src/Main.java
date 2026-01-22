import java.io.*;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }
        String inputFile = args[0];
        String outputFile = args[1];

        UserRegistry registry = new UserRegistry(500_000, 500_000);
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile),1 << 16)) {
            PrintWriter out = new PrintWriter(writer);
            SystemManager manager = new SystemManager(registry, out);
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) processCommand(line,manager,out);
            }
            out.flush();
        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
        }
    }

    private static void processCommand(String command,SystemManager manager ,PrintWriter out) {
        String[] p = command.split("\\s+");
        switch (p[0]) {
            case "register_customer":
                manager.registerCustomer(p);
                break;
            case "register_freelancer":
                manager.registerFreelancer(p);
                break;
            case "request_job":
                manager.requestJob(p);
                break;
            case "employ_freelancer":
                manager.employFreelancer(p);
                break;
            case "complete_and_rate":
                manager.completeAndRate(p);
                break;
            case "cancel_by_freelancer":
                manager.cancelByFreelancer(p);
                break;
            case "cancel_by_customer":
                manager.cancelByCustomer(p);
                break;
            case "blacklist":
                manager.blacklist(p);
                break;
            case "unblacklist":
                manager.unblacklist(p);
                break;
            case "change_service":
                manager.changeService(p);
                break;
            case "simulate_month":
                manager.simulateMonth();
                break;
            case "query_freelancer":
                manager.queryFreelancer(p);
                break;
            case "query_customer":
                manager.queryCustomer(p);
                break;
            case "update_skill":
                manager.updateSkill(p);
                break;
            default:
                out.println("Unknown command: " + p[0]);
        }
    }

}