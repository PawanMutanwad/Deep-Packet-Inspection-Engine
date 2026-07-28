package com.pawan.dpi;

import com.pawan.dpi.model.Packet;
import com.pawan.dpi.model.PcapGlobalHeader;
import com.pawan.dpi.parser.PcapReader;
import com.pawan.dpi.service.PacketProcessor;
import com.pawan.dpi.service.PcapWriter;

public class Main {

    public static void main(String[] args) {

        try {

            PcapReader reader = new PcapReader();
            PacketProcessor processor = new PacketProcessor();

            reader.open("input/sample.pcap");

            PcapGlobalHeader globalHeader = reader.readGlobalHeader();

            System.out.println(globalHeader);

            try (PcapWriter writer =
                         new PcapWriter("output/filtered.pcap", globalHeader)) {

                Packet packet;

                while ((packet = reader.readPacket()) != null) {

                    if (processor.process(packet)) {
                        writer.writePacket(packet);
                    }
                }
            }

            processor.printReport();

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}