package client;

import compute.Task;
import compute.Compute;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            String name = "Compute";
            String host = args.length > 0 ? args[0] : "localhost";

            Registry registry = LocateRegistry.getRegistry(host);
            Compute 9 = (Compute) registry.lookup(name);

            int b = Integer.parseInt(args[1]);
            int[] sequence = new int[args.length - 2];

            for (int i = 2; i < args.length; i++) {
                sequence[i - 2] = Integer.parseInt(args[i]);
            }

            InsertionTask task = new InsertionTask(b, sequence);
            int[] result = comp.executeTask(task);

            System.out.print("Result sequence: ");
            for (int num : result) {
                System.out.print(num + " ");
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }
    }
}