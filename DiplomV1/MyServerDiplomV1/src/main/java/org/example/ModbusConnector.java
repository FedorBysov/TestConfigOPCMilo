package org.example;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.ModbusUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ModbusConnector {
    private InetAddress ipAddress;
    private int port;
    private int timeout = 3000;  // Устанавливаем таймаут по умолчанию, например, 3000 миллисекунд (3 секунды)
    ModbusTCPMaster master = null;


    public ModbusConnector(String ip, int port) {
        try {
            this.ipAddress = InetAddress.getByName(ip);
            master = new ModbusTCPMaster(ip, port);
            master.setTimeout(timeout);  // Задаем таймаут соединения
            master.connect();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.port = port;
    }

//    public void connectM() throws Exception {
//
//        //master.(port);
//        //master = new ModbusTCPMaster((ipAddress).toString(), port);
//        master.setTimeout(timeout);  // Задаем таймаут соединения
//        master.connect();
//    }

    public void setTimeout(int timeout) {
        this.timeout = timeout; // Позволяет пользователю установить свой таймаут
    }

 //   public int[] readRegisters(int startAddress, int numOfRegs) throws Exception {
//        ReadInputRegistersRequest request = new ReadInputRegistersRequest(startAddress, numOfRegs);
//        ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
//        transaction.setRequest(request);
//        transaction.execute();
//
//        ModbusResponse response = transaction.getResponse();
//
//        if (response instanceof ReadInputRegistersResponse) {
//            ReadInputRegistersResponse inputResponse = (ReadInputRegistersResponse) response;
//
//            Register[] registers = (Register[]) inputResponse.getRegisters();  // Получаем массив регистров
//
//            int[] values = new int[registers.length];
//
//            for (int i = 0; i < registers.length; i++) {
//                values[i] = registers[i].toUnsignedShort();
//            }
//            return values;
//        } else {
//            throw new Exception("Received response is not of type ReadInputRegistersResponse");
//        }
//    }


    public double readDoubleValues(int ref, int count) throws Exception {

        try{

            Register[] registers = master.readMultipleRegisters(1, ref, count);

            if (registers.length >= 2) {
                byte[] bytes = new byte[4];
                bytes[0] =  (registers[0].toBytes()[0]);
                bytes[1] =  (registers[0].toBytes()[1]);
                bytes[2] =  (registers[1].toBytes()[0]);
                bytes[3] =  (registers[1].toBytes()[1]);

            return ModbusUtil.registersToFloat(bytes);
        } else {
            throw new RuntimeException("Недостаточное количество регистров для формирования значения типа double");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

        return Double.NaN;
}


        //        int numOfRegs = 64; // Для 64-битного double требуются 4 регистра (32 бита на каждые 2 регистра)
//        int[] values = readRegisters(startAddress, numOfRegs);
//
//        // Конвертация 4 регистров в значение типа double
//        long high = (long) values[0] << 16 | values[1];
//        long low = (long) values[2] << 16 | values[3];
//
//        long combined = high << 32 | low;
//        return Double.longBitsToDouble(combined);
//    }


//    public void writeRegisters(int startAddress, int[] values) throws Exception {
//        WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(startAddress, values);
//        transaction = new ModbusTCPTransaction(connection);
//        transaction.setRequest(request);
//        transaction.execute();
//        WriteMultipleRegistersResponse response = (WriteMultipleRegistersResponse) transaction.getResponse();
//    }

//    public void writeRegisters(int startAddress, int[] values) throws Exception {
//        // Создаем массив регистров из ваших значений int
//        Register[] registers = new Register[values.length];
//        for (int i = 0; i < values.length; i++) {
//            registers[i] = new SimpleRegister(values[i]);
//        }
//
//        // Создаем запрос на запись с этим массивом регистров
//        WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(startAddress, registers);
//        ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
//        transaction.setRequest(request);
//        transaction.execute();
//
//        // Получаем и проверяем ответ
//        WriteMultipleRegistersResponse response = (WriteMultipleRegistersResponse) transaction.getResponse();
//        if (response == null) {
//            throw new Exception("Response is null");
//        }
//    }



    public void disconnect() {
        master.disconnect();
    }
}

