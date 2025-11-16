package com.example.bai2.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.example.bai2.model.Customer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class XmlUtils {

    private static final String TAG = "XmlUtils";
    private static final String TAG_CUSTOMERS = "customers";
    private static final String TAG_CUSTOMER = "customer";
    private static final String TAG_PHONE = "phone";
    private static final String TAG_NAME = "name";
    private static final String TAG_POINTS = "points";
    private static final String TAG_CREATED_AT = "createdAt";
    private static final String TAG_UPDATED_AT = "updatedAt";

    /**
     * Ghi file XML ra thư mục .../files/Download/Bai2/
     */
    public static File writeXmlToDownloads(Context context, List<Customer> customers) {
        try {
            // Lấy thư mục (Download không có 's' là đúng theo Logcat của bạn)
            File baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File appDir = new File(baseDir, "Bai2");
            if (!appDir.exists()) {
                appDir.mkdirs();
            }

            // Tạo timestamp (ví dụ: 20251116_105900)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = sdf.format(new Date());

            // Tạo tên file duy nhất
            String fileName = "customers_export_" + timestamp + ".xml";

            // Tạo file với tên duy nhất
            File file = new File(appDir, "customers_export_2.xml");

            // Toàn bộ code ghi XML
            StringWriter stringWriter = new StringWriter();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(stringWriter);

            serializer.startDocument("UTF-8", true);
            serializer.startTag(null, TAG_CUSTOMERS);

            for (Customer c : customers) {
                serializer.startTag(null, TAG_CUSTOMER);
                writeXmlTag(serializer, TAG_PHONE, c.getPhone());
                writeXmlTag(serializer, TAG_NAME, c.getName());
                writeXmlTag(serializer, TAG_POINTS, String.valueOf(c.getPoints()));
                writeXmlTag(serializer, TAG_CREATED_AT, c.getCreatedAt());
                writeXmlTag(serializer, TAG_UPDATED_AT, c.getUpdatedAt());
                serializer.endTag(null, TAG_CUSTOMER);
            }

            serializer.endTag(null, TAG_CUSTOMERS);
            serializer.endDocument();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(stringWriter.toString().getBytes());
            fos.close();

            Log.d(TAG, "XML file written successfully to: " + file.getAbsolutePath());
            return file;

        } catch (Exception e) {
            Log.e(TAG, "Error writing XML file", e);
            return null;
        }
    }

    /**
     * Đọc file XML từ một FileInputStream (để Import)
     */
    public static List<Customer> parseCustomersXml(FileInputStream inputStream) {
        List<Customer> customers = new ArrayList<>();
        Customer currentCustomer = null;
        String text = "";

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equalsIgnoreCase(TAG_CUSTOMER)) {
                            currentCustomer = new Customer();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (currentCustomer != null) {
                            if (tagName.equalsIgnoreCase(TAG_CUSTOMER)) {
                                customers.add(currentCustomer);
                            } else if (tagName.equalsIgnoreCase(TAG_PHONE)) {
                                currentCustomer.setPhone(text);
                            } else if (tagName.equalsIgnoreCase(TAG_NAME)) {
                                currentCustomer.setName(text);
                            } else if (tagName.equalsIgnoreCase(TAG_POINTS)) {
                                currentCustomer.setPoints(Integer.parseInt(text));
                            } else if (tagName.equalsIgnoreCase(TAG_CREATED_AT)) {
                                currentCustomer.setCreatedAt(text);
                            } else if (tagName.equalsIgnoreCase(TAG_UPDATED_AT)) {
                                currentCustomer.setUpdatedAt(text);
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing XML", e);
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
        Log.d(TAG, "Parsed " + customers.size() + " customers from XML");
        return customers;
    }

    /**
     * Hàm tiện ích (Hàm này cũng có thể bị lỗi)
     */
    private static void writeXmlTag(XmlSerializer serializer, String tagName, String text) throws Exception {
        if (text == null) text = "";
        serializer.startTag(null, tagName);
        serializer.text(text);
        serializer.endTag(null, tagName);
    }
}