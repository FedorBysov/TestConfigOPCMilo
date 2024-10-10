package org.example;

import java.net.InetAddress;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.ModbusUtil;

public class modbusClientTest {

    // Чтение регистров и преобразование в значение double
    public static double readRegistersAsDouble(String address, int port, int unitId, int ref, int count) {
        ModbusTCPMaster master = null;
        try {
            master = new ModbusTCPMaster(address, port);
            master.connect();
            Register[] registers = master.readMultipleRegisters(unitId, ref, count);

            if (registers.length >= 2) {
                byte[] bytes = new byte[4];
                bytes[0] = (byte) (registers[0].toBytes()[0]);
                bytes[1] = (byte) (registers[0].toBytes()[1]);
                bytes[2] = (byte) (registers[1].toBytes()[0]);
                bytes[3] = (byte) (registers[1].toBytes()[1]);

                return ModbusUtil.registersToFloat(bytes);
            } else {
                throw new RuntimeException("Недостаточное количество регистров для формирования значения типа double");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (master != null) {
                master.disconnect();
            }
        }
        return Double.NaN;
    }

    public static void main(String[] args) {
        double value = readRegistersAsDouble("WIN-A5SE79QKVGU", 502, 1 , 0, 4);
        System.out.println("Прочитанное значение: " + value);
    }
}
