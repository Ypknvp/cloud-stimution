import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class SimpleCloudSimFCFSExample {

    public static void main(String[] args) {

        try {
            int numUsers = 1;   // Number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            // Initialize the CloudSim library
            CloudSim.init(numUsers, calendar, traceFlag);

            // Create Datacenter
            Datacenter datacenter = createDatacenter("Datacenter_0");

            // Create Broker
            DatacenterBroker broker = new DatacenterBroker("Broker");
            int brokerId = broker.getId();

            // Create VMs and Cloudlets and send them to the broker
            int numVMs = 4;
            int numCloudlets = 8;

            List<Vm> vmList = createVMs(brokerId, numVMs);
            List<Cloudlet> cloudletList = createCloudlets(brokerId, numCloudlets);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            // First-Come, First-Served (FCFS) Cloudlet allocation
            int currentVmIndex = 0;
            for (Cloudlet cloudlet : cloudletList) {
                cloudlet.setVmId(vmList.get(currentVmIndex).getId());
                currentVmIndex = (currentVmIndex + 1) % vmList.size(); // Cycle through VMs
            }

            // Start the simulation
            CloudSim.startSimulation();

            // Stop the simulation
            CloudSim.stopSimulation();

            // Print results
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            
            // Display the output header "Yogesh.P 22IT137"
            System.out.println("Yogesh.P 22IT137\n");
            printCloudletList(newList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter(String name) {

        List<Host> hostList = new ArrayList<Host>();

        int mips = 1000;
        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;

        // Create Host with processing elements (PE)
        List<Pe> peList = new ArrayList<Pe>();
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        hostList.add(new Host(0, new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw), storage, peList,
                new VmSchedulerTimeShared(peList)));

        return new Datacenter(name, hostList, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
    }

    private static List<Vm> createVMs(int brokerId, int numVMs) {
        List<Vm> vmList = new ArrayList<Vm>();

        int mips = 1000;
        long size = 10000; // image size (MB)
        int ram = 512; // vm memory (MB)
        int bw = 1000;
        int pesNumber = 1; // number of CPUs
        String vmm = "Xen"; // VMM name

        for (int i = 0; i < numVMs; i++) {
            Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }
        return vmList;
    }

    private static List<Cloudlet> createCloudlets(int brokerId, int numCloudlets) {
        List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();

        long length = 40000;
        int pesNumber = 1;
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < numCloudlets; i++) {
            Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
        }
        return cloudletList;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        String indent = "    ";
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            System.out.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                System.out.println("SUCCESS" + indent + indent + cloudlet.getResourceId() + indent + indent + cloudlet.getVmId() + indent + indent + cloudlet.getActualCPUTime() + indent + indent + cloudlet.getExecStartTime() + indent + indent + cloudlet.getFinishTime());
            }
        }
    }
}
