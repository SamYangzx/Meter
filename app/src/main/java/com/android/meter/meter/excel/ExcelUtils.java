package com.android.meter.meter.excel;

import com.android.meter.meter.util.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {
    private static final String TAG = ExcelUtils.class.getSimpleName();
    private static final int ROW_OFFSET_EVERYTIME = 5;

    public static WritableFont arial14font = null;

    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;

    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";

    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14,
                    WritableFont.BOLD);
//			arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);
            arial10font = new WritableFont(WritableFont.ARIAL, 10,
                    WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
//            arial10format.setBackground(jxl.format.Colour.LIGHT_BLUE);
            arial12font = new WritableFont(WritableFont.ARIAL, 12);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
        } catch (WriteException e) {

            e.printStackTrace();
        }
    }

    public static void initExcel(String fileName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        WritableSheet sheet;
        try {
            File file = new File(fileName);
            if (!file.exists()) {    //源文件不存在时创建，存在时追加。
                file.createNewFile();
                workbook = Workbook.createWorkbook(file);
                sheet = workbook.createSheet("测量数据表", 0);
            } else {
                Workbook wb = Workbook.getWorkbook(file);
                workbook = Workbook.createWorkbook(file, wb);
                sheet = workbook.getSheet(0);
            }
            if (sheet.getRows() == 0) {
                sheet.addCell((WritableCell) new Label(0, 0, fileName,
                        arial14format));
                for (int col = 0; col < colName.length; col++) {
                    sheet.addCell(new Label(col, 0, colName[col], arial10format));
                }
            }
            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    public static <T> void writeObjListToExcel(List<T> objList,
                                               String fileName, boolean needOffset) {
        LogUtil.d(TAG, "writeObjListToExcel.invoke");
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(new File(fileName));
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(new File(fileName),
                        workbook);
                WritableSheet sheet = writebook.getSheet(0);

                int currentRows = sheet.getRows();
                for (int j = 0; j < objList.size(); j++) {
                    ArrayList<String> list = (ArrayList<String>) objList.get(j);
                    LogUtil.d(TAG, "writeObjToRow.getRow: " + currentRows + ", needOffset: " + needOffset);
                    if (needOffset && currentRows != 1 && j == 0) { //当仅有标题时不需要进行偏移。
                        currentRows += ROW_OFFSET_EVERYTIME;
                    }
                    for (int i = 0; i < list.size(); i++) {
                        sheet.addCell(new Label(i, j + currentRows, list.get(i),
                                arial12format));
                    }
                }
                writebook.write();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }


    public static int getRows(String fileName) {
        WritableWorkbook writebook = null;
        InputStream in = null;
        Workbook workbook = null;
        int currentRows = 0;
        try {
            WorkbookSettings setEncode = new WorkbookSettings();
            setEncode.setEncoding(UTF8_ENCODING);
            in = new FileInputStream(new File(fileName));
            workbook = Workbook.getWorkbook(in);
            writebook = Workbook.createWorkbook(new File(fileName),
                    workbook);
            WritableSheet sheet = writebook.getSheet(0);

            currentRows = sheet.getRows();
            writebook.write();  //打开后，必须执行下此操作，不然后面会出错。
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                workbook.close();
            }

            if (writebook != null) {
                try {
                    writebook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return currentRows;
    }

    /**
     * 将数据写到指定的行。
     *
     * @param objList
     * @param fileName
     * @param writeRow
     */
    public static void writeObjToRow(List<String> objList,
                                     String fileName, int writeRow) {
        LogUtil.d(TAG, "writeObjToRow.writeRow: " + writeRow);
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(new File(fileName));
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(new File(fileName),
                        workbook);
                WritableSheet sheet = writebook.getSheet(0);
                ArrayList<String> list = (ArrayList<String>) objList;
                for (int i = 0; i < list.size(); i++) {
                    sheet.addCell(new Label(i, writeRow - 1, list.get(i), //首行为０行。
                            arial12format));
                }
                writebook.write();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public static Object getValueByRef(Class cls, String fieldName) {
        Object value = null;
        fieldName = fieldName.replaceFirst(fieldName.substring(0, 1), fieldName
                .substring(0, 1).toUpperCase());
        String getMethodName = "get" + fieldName;
        try {
            Method method = cls.getMethod(getMethodName);
            value = method.invoke(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
