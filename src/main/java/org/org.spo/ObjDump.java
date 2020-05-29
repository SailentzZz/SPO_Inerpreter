package org.org.spo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class ObjDump {

    public static void main(String[] args) throws IOException {
        String fileName = args.length > 0 ? args[0] : "src\\main\\resources\\example.obj";
        try (RandomAccessFile file = new RandomAccessFile(fileName, "r")) {
            int symbolTablePtr; // смещение начала таблицы символов в файле
            int symbolCount; // количество записей в таблице символов
            {
                byte[] header = new byte[20];
                file.read(header); // читаем заголовок файла - первые 20 байт
                symbolTablePtr = getInt32(header, 8);
                symbolCount = getInt32(header, 12);
            }

            byte[] stringTable; // таблица имен (без первых 4 байт размера)
            {
                int stringTablePtr = symbolTablePtr + 18 * symbolCount; // смещение начала таблицы имен
                file.seek(stringTablePtr); // переходим к началу таблицы имен
                byte[] stringTableSizeBytes = new byte[4];
                file.read(stringTableSizeBytes); // читаем размер таблицы имен
                int stringTableSize = getInt32(stringTableSizeBytes, 0);
                stringTable = new byte[stringTableSize];
                file.read(stringTable); // читаем саму таблицу имен
            }

            System.out.printf("Symbols in %s:\n", fileName);
            file.seek(symbolTablePtr); // переходим к началу таблицы символов
            int aux = 0; // текущее количество непрочитанных дополнительных записей
            for (int i = 0; i < symbolCount; i++) {
                byte[] symbol = new byte[18];
                file.read(symbol); // читаем запись из таблицы символов
                if (aux > 0) {
                    // Еще есть дополнительные записи, пропускаем
                    aux--;
                } else {
                    // Это главная запись, печатаем имя и секцию
                    String name = getSymbolName(symbol, stringTable);
                    short section = getInt16(symbol, 12);
                    System.out.printf("%s, section %d\n", name, section);
                    aux = symbol[17] & 0xFF; // определяем количество дополнительных записей, которые нужно пропустить
                }
            }
        }
    }

    //Возвращает little endian 32-битное целое число, содержащееся в 4 байтах массива array, начиная с индекса offset
    private static int getInt32(byte[] array, int offset) {
        return ByteBuffer.wrap(array, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    //Возвращает little endian 16-битное целое число, содержащееся в 2 байтах массива array, начиная с индекса offset
    private static short getInt16(byte[] array, int offset) {
        return ByteBuffer.wrap(array, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    //Определяет имя символа, исходя из записи в таблице символов и таблицы имен
    private static String getSymbolName(byte[] symbol, byte[] stringTable) {
        if (getInt32(symbol, 0) != 0) {
            // Первые 4 байта содержат ненулевые значения, это имя длиной не более 8 символов.
            // Определяем последний ненулевой байт:
            for (int i = 7; i >= 0; i--) {
                if (symbol[i] != 0) {
                    return new String(symbol, 0, i + 1, StandardCharsets.US_ASCII);
                }
            }

        } else {
            // Первые 4 байта нулевые, вторые 4 байта - смещение имени от начала таблицы имен:
            int stringTableOffset = getInt32(symbol, 4) - 4; // "- 4" из-за того, что не учитываем 4 байта размера
            // Ищем завершающий нулевой байт:
            for (int i = stringTableOffset; i < stringTable.length; i++) {
                if (stringTable[i] == 0) {
                    return new String(stringTable, stringTableOffset, i - stringTableOffset, StandardCharsets.US_ASCII);
                }
            }
        }

        throw new IllegalArgumentException();
    }
}
