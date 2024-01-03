import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.ModbusSlave;
import com.intelligt.modbus.jlibmodbus.ModbusSlaveFactory;
import com.intelligt.modbus.jlibmodbus.data.DataHolder;
import com.intelligt.modbus.jlibmodbus.data.ModbusCoils;
import com.intelligt.modbus.jlibmodbus.data.ModbusHoldingRegisters;
import com.intelligt.modbus.jlibmodbus.exception.IllegalDataAddressException;
import com.intelligt.modbus.jlibmodbus.exception.IllegalDataValueException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.utils.FrameEvent;
import com.intelligt.modbus.jlibmodbus.utils.FrameEventListener;
import com.intelligt.modbus.jlibmodbus.utils.DataUtils;
import com.intelligt.modbus.jlibmodbus.serial.*;
import com.intelligt.modbus.jlibmodbus.exception.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import jssc.SerialPortList;
import java.util.ArrayList;
import java.util.List;

public class MainSlave2 {
    static public void main(String[] arg) {
        try {
            Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);
            SerialParameters serialParameters = new SerialParameters();
            SerialUtils.setSerialPortFactory(new SerialPortFactoryLoopback(false));
            ModbusSlave slave = ModbusSlaveFactory.createModbusSlaveRTU(serialParameters);
            slave.setBroadcastEnabled(true);
            slave.setReadTimeout(10000);
            serialParameters.setDevice(SerialPortList.getPortNames()[0]);
            //Для жеки можно типо SerialPortList.getPortNames()[1] чтоб красива жиес
            serialParameters.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600);
            serialParameters.setParity(SerialPort.Parity.NONE);
            serialParameters.setDataBits(8);
            serialParameters.setStopBits(1);
            DataHolder dataHolder = slave.getDataHolder();
            ModbusHoldingRegisters holdingRegisters = new ModbusHoldingRegisters(1000);
            for (int i = 0; i < holdingRegisters.getQuantity(); i++) {
                holdingRegisters.set(i, i + 1);
            }
            holdingRegisters.setFloat64At(0, Math.PI);//пи в нулевом
            ModbusCoils coils = new ModbusCoils(1000);
            for (int i = 0; i < coils.getQuantity(); i++) {
                if(i % 2 == 0) {
                    coils.set(i, true);//в каждом четном тру
                }else{
                    coils.set(i, false);//в каждом нечетном фолс
                }
            }
            ModbusHoldingRegisters inputRegisters = dataHolder.getInputRegisters();
            for (int i = 0; i < inputRegisters.getQuantity(); i++) {
                inputRegisters.set(i, i + 1);
            }
            ModbusCoils discreteInputs = dataHolder.getDiscreteInputs();
            for (int i = 0; i < discreteInputs.getQuantity(); i++) {
                if (i % 2 == 0) {
                    discreteInputs.set(i, true);//в каждом четном тру
                } else {
                    discreteInputs.set(i, false);//в каждом нечетном фолс
                }
            }
            slave.listen();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}