package org.example;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
//import com.ghgande.j2mod.modbus.util.ModbusException;

public class ModbusConnectorTester {

    private String host;
    private int port;

    /**
     * Конструктор класса для проверки соединения Modbus.
     * @param host Хост устройства Modbus
     * @param port Порт для подключения
     */
    public ModbusConnectorTester(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Проверяет соединение, выполняя чтение регистра.
     * @param unitId ID устройства Modbus
     * @param ref Адрес регистра для чтения
     * @param count Количество регистров для чтения
     * @return true, если соединение успешно, иначе false
     */
    public boolean testConnection(int unitId, int ref, int count) {
        ModbusTCPMaster master = null;
        try {
            master = new ModbusTCPMaster(host, port);
            master.connect();

            // Простое чтение регистров, игнорируем полученные данные
            master.readMultipleRegisters(unitId, ref, count);

            // Если ошибок нет, соединение успешное
            return true;
        } catch (Exception e) {
            // Логгируем ошибку или просто печатаем стек вызова
            e.printStackTrace();
            return false;
        } finally {
            if (master != null) {
                master.disconnect();
            }
        }
    }

    // Геттеры и сеттеры при необходимости
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        ModbusConnectorTester tester = new ModbusConnectorTester("WIN-A5SE79QKVGU", 502);
        boolean isConnected = tester.testConnection(1, 4, 1); // ID устройства, адрес регистра, количество регистров для чтения
        System.out.println("Connection successful: " + isConnected);
    }
}
