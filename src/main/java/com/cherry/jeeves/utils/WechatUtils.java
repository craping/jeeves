package com.cherry.jeeves.utils;

import com.cherry.jeeves.domain.response.component.WechatHttpResponseBase;
import com.cherry.jeeves.domain.shared.Contact;
import com.cherry.jeeves.exception.WechatException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WechatUtils {
    public static void checkBaseResponse(WechatHttpResponseBase response) {
        if (response.getBaseResponse().getRet() != 0) {
            throw new WechatException(response.getClass().getSimpleName() + " ret = " + response.getBaseResponse().getRet());
        }
    }

    public static String textDecode(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text");
        }
        return new String(text.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    public static boolean isIndividual(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact");
        }
        return contact.getUserName().startsWith("@") && !contact.getUserName().startsWith("@@") && ((contact.getVerifyFlag() & 8) == 0);
    }

    public static boolean isChatRoom(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact");
        }
        return contact.getUserName().startsWith("@@");
    }

    public static boolean isMediaPlatform(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact");
        }
        return contact.getUserName().startsWith("@") && !contact.getUserName().startsWith("@@") && ((contact.getVerifyFlag() & 8) > 0);
    }
    
    /**
     * 从文件中获取二进制数据
     *
     * @param file 文件
     * @return 二进制数据
     */
    public static byte[] getBytes(File file) {
        if (file == null) {
            throw new IllegalArgumentException("参数file不能为null");
        }

        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = new FileInputStream(file);
            baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;

            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }

            baos.flush();
            baos.close();
            fis.close();

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
