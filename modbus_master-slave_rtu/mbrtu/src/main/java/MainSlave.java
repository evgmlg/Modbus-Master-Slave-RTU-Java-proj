import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.data.DataHolder;
import com.intelligt.modbus.jlibmodbus.data.ModbusHoldingRegisters;
import com.intelligt.modbus.jlibmodbus.data.ModbusCoils;
import com.intelligt.modbus.jlibmodbus.exception.IllegalDataAddressException;
import com.intelligt.modbus.jlibmodbus.exception.IllegalDataValueException;
import com.intelligt.modbus.jlibmodbus.ModbusSlave;
import com.intelligt.modbus.jlibmodbus.ModbusSlaveFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import com.intelligt.modbus.jlibmodbus.serial.*;
import jssc.SerialPortList;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainSlave {

    static public void main(String[] argv) {

        try {

            final ModbusSlave slave;

            SerialParameters serialParameters = new SerialParameters();

            serialParameters.setDevice("COM3");
            serialParameters.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600);
            serialParameters.setDataBits(8);
            serialParameters.setParity(SerialPort.Parity.NONE);
            serialParameters.setStopBits(1);

            slave = ModbusSlaveFactory.createModbusSlaveRTU(serialParameters);
            slave.setReadTimeout(0); // if not set default timeout is 1000ms, I think this must be set to 0 (infinitive timeout)
            Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);

            MyOwnDataHolder dh = new MyOwnDataHolder();
            dh.addEventListener(new ModbusEventListener() {
                @Override
                public void onWriteToSingleCoil(int address, boolean value) {
                    System.out.print("onWriteToSingleCoil: address " + address + ", value " + value);
                }

                @Override
                public void onWriteToMultipleCoils(int address, int quantity, boolean[] values) {
                    System.out.print("onWriteToMultipleCoils: address " + address + ", quantity " + quantity);
                }

                @Override
                public void onWriteToSingleHoldingRegister(int address, int value) {
                    System.out.print("onWriteToSingleHoldingRegister: address " + address + ", value " + value);
                }

                @Override
                public void onWriteToMultipleHoldingRegisters(int address, int quantity, int[] values) {
                    System.out.print("onWriteToMultipleHoldingRegisters: address " + address + ", quantity " + quantity);
                }
            });

            slave.setDataHolder(dh);
            ModbusHoldingRegisters hr = new ModbusHoldingRegisters(10);
            hr.set(0, 12345);
            hr.set(1, 1234);
            slave.getDataHolder().setHoldingRegisters(hr);
            ModbusCoils cl = new ModbusCoils(10);
            cl.set(0,true);
            cl.set(1,false);
            cl.set(2,false);
            cl.set(3,true);
            cl.set(4,true);
            cl.set(5,false);
            cl.set(6,true);
            cl.set(7,false);
            slave.getDataHolder().setCoils(cl);
            ModbusCoils ds = dh.getDiscreteInputs();
            ds.set(0,true);
            ds.set(1,true);
            ds.set(2,false);
            slave.getDataHolder().setDiscreteInputs(ds);
            ModbusHoldingRegisters ir = dh.getInputRegisters();
            ir.set(0,432);
            ir.set(1,234);
            slave.getDataHolder().setInputRegisters(ir);
            slave.setServerAddress(1);
            /*
             * using master-branch it should be #slave.open();
             */
            slave.listen();

            /*
             * since 1.2.8
             */
            if (slave.isListening()) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        synchronized (slave) {
                            slave.notifyAll();
                        }
                    }
                });

                synchronized (slave) {
                    slave.wait();
                }

                /*
                 * using master-branch it should be #slave.close();
                 */
                slave.shutdown();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface ModbusEventListener {
        void onWriteToSingleCoil(int address, boolean value);

        void onWriteToMultipleCoils(int address, int quantity, boolean[] values);

        void onWriteToSingleHoldingRegister(int address, int value);

        void onWriteToMultipleHoldingRegisters(int address, int quantity, int[] values);
    }

    public static class MyOwnDataHolder extends DataHolder {

        final List<ModbusEventListener> modbusEventListenerList = new ArrayList<ModbusEventListener>();

        public MyOwnDataHolder() {

            setHoldingRegisters(new ModbusHoldingRegisters(10));
            setCoils(new ModbusCoils(128));
            setDiscreteInputs(new ModbusCoils(128));
            setInputRegisters(new ModbusHoldingRegisters(10));
        }

        public void addEventListener(ModbusEventListener listener) {
            modbusEventListenerList.add(listener);
        }

        public boolean removeEventListener(ModbusEventListener listener) {
            return modbusEventListenerList.remove(listener);
        }

        @Override
        public void writeHoldingRegister(int offset, int value) throws IllegalDataAddressException, IllegalDataValueException {
            for (ModbusEventListener l : modbusEventListenerList) {
                l.onWriteToSingleHoldingRegister(offset, value);
            }
            super.writeHoldingRegister(offset, value);
        }

        @Override
        public void writeHoldingRegisterRange(int offset, int[] range) throws IllegalDataAddressException, IllegalDataValueException {
            for (ModbusEventListener l : modbusEventListenerList) {
                l.onWriteToMultipleHoldingRegisters(offset, range.length, range);
            }
            super.writeHoldingRegisterRange(offset, range);
        }

        @Override
        public void writeCoil(int offset, boolean value) throws IllegalDataAddressException, IllegalDataValueException {
            for (ModbusEventListener l : modbusEventListenerList) {
                l.onWriteToSingleCoil(offset, value);
            }
            super.writeCoil(offset, value);
        }

        @Override
        public void writeCoilRange(int offset, boolean[] range) throws IllegalDataAddressException, IllegalDataValueException {
            for (ModbusEventListener l : modbusEventListenerList) {
                l.onWriteToMultipleCoils(offset, range.length, range);
            }
            super.writeCoilRange(offset, range);
        }
    }
}