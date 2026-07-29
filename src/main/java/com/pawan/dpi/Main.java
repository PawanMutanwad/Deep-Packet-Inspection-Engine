package com.pawan.dpi;

import com.pawan.dpi.model.Packet;
import com.pawan.dpi.model.PacketHeader;
import com.pawan.dpi.model.PcapGlobalHeader;
import com.pawan.dpi.parser.PcapReader;
import com.pawan.dpi.service.PacketProcessor;
import com.pawan.dpi.service.PcapWriter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    // Poison pill to signal consumer worker threads to terminate cleanly
    private static final Packet POISON_PILL = new Packet(new PacketHeader(0, 0, 0, 0), new byte[0]);
    private static final int QUEUE_CAPACITY = 8192;
    private static final int NUM_WORKER_THREADS = 4;

    public static void main(String[] args) {

        System.out.println("Starting Concurrent Deep Packet Inspection Engine...");

        try {

            PcapReader reader = new PcapReader();
            PacketProcessor processor = new PacketProcessor();

            reader.open("input/sample.pcap");

            PcapGlobalHeader globalHeader = reader.readGlobalHeader();

            System.out.println(globalHeader);

            try (PcapWriter writer = new PcapWriter("output/filtered.pcap", globalHeader)) {

                BlockingQueue<Packet> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
                ExecutorService executor = Executors.newFixedThreadPool(NUM_WORKER_THREADS);

                // Launch worker threads (Consumers)
                for (int i = 0; i < NUM_WORKER_THREADS; i++) {
                    executor.submit(() -> {
                        try {
                            while (true) {
                                Packet packet = queue.take();

                                if (packet == POISON_PILL) {
                                    break;
                                }

                                if (processor.process(packet)) {
                                    writer.writePacket(packet);
                                }
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                // Producer: Reader loop
                Packet packet;
                while ((packet = reader.readPacket()) != null) {
                    queue.put(packet);
                }

                // Send poison pills to stop all worker threads
                for (int i = 0; i < NUM_WORKER_THREADS; i++) {
                    queue.put(POISON_PILL);
                }

                executor.shutdown();
                boolean finished = executor.awaitTermination(60, TimeUnit.SECONDS);

                if (!finished) {
                    System.err.println("Warning: Worker threads timed out during shutdown.");
                }
            }

            processor.printReport();

            reader.close();

            System.out.println("Processing completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}