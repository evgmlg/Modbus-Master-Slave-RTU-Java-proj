import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.serial.*;
import jssc.SerialPortList;

import java.util.Scanner;

public class main {

    static public void main(String[] arg) {
        SerialParameters sp = new SerialParameters();
        Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);

        try {
            //Вывод доступных портов
            String[] dev_list = SerialPortList.getPortNames();


            System.out.println("Доступные порты:");

            for (int n = 0; n < dev_list.length; n++) {
                System.out.println(dev_list[n]);
            }

            if (dev_list.length > 0) {
                //Настройка подключения к порту и передачи данных
                sp.setDevice("COM6");
                sp.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600);
                sp.setDataBits(8);
                sp.setParity(SerialPort.Parity.NONE);
                sp.setStopBits(1);

                //Создание и подключение через jssc
                SerialUtils.setSerialPortFactory(new SerialPortFactoryJSSC());
                ModbusMaster m = ModbusMasterFactory.createModbusMasterRTU(sp);

                m.connect();

                //Настройка
                int slaveId = 1; // id устройства slave
                int offset = 0; // Начальный адресс
                int quantity1 = 8; //Количество условных портов
                int quantity2 = 3;
                int quantity3 = 2;
                int quantity4 = 2;

                Scanner sc = new Scanner(System.in);

                try {

                    System.out.println("Выберите режим: \n"+"1. Чтение регистра флага(Read Coils)\n"+
                    "2. Запись регистра флага(Write Coils)\n"+"3. Чтение дискретного входа(Read Discrete Inputs)\n"+
                    "4. Чтение регистра входа(Read Input Registers)\n"+"5. Чтение регистра хранения(Read Holding Registers)\n"+
                    "6. Запись регистра хранения(Write Holding Registers)\n");

                    int swc = sc.nextInt();

                    switch (swc){
                        case 1:
                            boolean[] regCoils = m.readCoils(slaveId, offset, quantity1);
                            for(boolean val1 : regCoils){
                                System.out.println("Адресс флага: "+offset+", Значения флага: "+ val1);
                                offset++;
                                if(offset == quantity1){
                                    break;
                                }
                            }
                            break;

                        case 2:
                            System.out.println("Выберите Single или Multiple coils запись: \n" + "1. Запись Single Coil\n"+
                            "2. Запись Multiple Coils\n");

                            Scanner sc2 = new Scanner(System.in);
                            int coilsc = sc2.nextInt();
                            switch (coilsc){
                                case 1:
                                    m.writeSingleCoil(slaveId, 0, true);
                                    boolean[] wregCoils = m.readCoils(slaveId, offset, quantity1);
                                    for(boolean val2 : wregCoils){
                                        System.out.println("Адресс флага: "+offset+", Значения флага: "+ val2);
                                        offset++;
                                        if(offset == quantity1){
                                            break;
                                        }
                                    }
                                    break;
                                case 2:
                                    m.writeMultipleCoils(1,0,new boolean[]{true,true,true,true,true,true,true,true});
                                    boolean[] wregMulCoils = m.readCoils(slaveId, offset, quantity1);
                                    for(boolean val2 : wregMulCoils){
                                        System.out.println("Адресс флага: "+offset+", Значения флага: "+ val2);
                                        offset++;
                                        if(offset == quantity1){
                                            break;
                                        }
                                    }
                                    break;
                                default:
                                    System.out.println("Введите верное число!\n");
                                    break;
                            }
                            break;

                        case 3:
                            boolean[] readDisc = m.readDiscreteInputs(slaveId, offset, quantity2);
                            for (boolean val3 : readDisc){
                                System.out.println("Адрес: "+offset + ", Значение: "+ val3);
                                offset++;
                                if(offset == quantity2){
                                    break;
                                }
                            }
                            break;

                        case 4:
                            int[] readInp = m.readInputRegisters(slaveId, offset, quantity3);
                            for (int val4 : readInp){
                                System.out.println("Адрес: "+offset + ", Значение: "+ val4);
                                offset++;
                                if(offset == quantity3){
                                    break;
                                }
                            }
                            break;

                        case 5:
                            int[] regVal = m.readHoldingRegisters(slaveId, offset, quantity4);
                            for (int val5 : regVal){
                                System.out.println("Адрес: "+offset + ", Значение: "+ val5);
                                offset++;
                                if(offset == quantity4){
                                    break;
                                }
                            }
                            break;
                        case 6:
                            System.out.println("Выберите Single или Multiple Holding Registers запись: \n" + "1. Запись Single Holding Register\n"+
                            "2. Запись Multiple Holding Registers\n");
                            Scanner sc3 = new Scanner(System.in);
                            int hregsc = sc3.nextInt();
                            switch (hregsc){
                                case 1:
                                    m.writeSingleRegister(1, 0, 15);
                                    int[] wSinregVal = m.readHoldingRegisters(slaveId, offset, quantity4);
                                    for (int val6 : wSinregVal){
                                        System.out.println("Адрес: "+offset + ", Значение: "+ val6);
                                        offset++;
                                        if(offset == quantity4){
                                            break;
                                        }
                                    }
                                    break;
                                case 2:
                                    m.writeMultipleRegisters(1, 0, new int[]{37, 73});
                                    int[] wMulregVal = m.readHoldingRegisters(slaveId, offset, quantity4);
                                    for (int val6 : wMulregVal){
                                        System.out.println("Адрес: "+offset + ", Значение: "+ val6);
                                        offset++;
                                        if(offset == quantity4){
                                            break;
                                        }
                                    }
                                    break;
                            }
                            break;
                        default:
                            System.out.println("Введите цифру из списка!\n");
                    }

                } catch (ModbusIOException mioE) {
                    Modbus.log().throwing("???", "???", mioE);
                } catch (Exception e) {
                    Modbus.log().throwing("???", "???", e);
                }
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
