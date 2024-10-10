package org.example;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;

import java.net.InetAddress;

public class Main {
    public static double readDoubleFromModbus(String ipAddress, int port, int unitId, int ref, int count) {
        TCPMasterConnection connection = null;
        ModbusTCPTransaction transaction = null;
        double result = 0.0;

        try {
            // Адрес и порт сервера
            InetAddress address = InetAddress.getByName(ipAddress);
            connection = new TCPMasterConnection(address);
            connection.setPort(port);
            connection.connect();

            // Создаем запрос
            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(ref, count);
            request.setUnitID(unitId);

            // Транзакция
            transaction = new ModbusTCPTransaction(connection);
            transaction.setRequest(request);
            transaction.execute();

            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            int value1 = response.getRegisterValue(0);
            int value2 = response.getRegisterValue(1);

            long combined = ((long)value1 << 16) + value2;
            result = Double.longBitsToDouble(combined);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        return result;
    }

    public static void main(String[] args) {
        String ip = "WIN-A5SE79QKVGU"; // IP Modbus сервера
        int port =502;
        int unitId = 1;
        int startRegister = 1; // Пример адреса
        int numRegisters = 4; // Два регистра для 64-битного double

        //double temperature = readDoubleFromModbus(ip, port, unitId, startRegister, numRegisters);
        //System.out.println("Считанное значение: " + temperature);
    }
}
