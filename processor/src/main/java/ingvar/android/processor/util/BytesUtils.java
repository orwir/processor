package ingvar.android.processor.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class BytesUtils {

    public static byte[] toBytes(Object object) {
        byte[] result = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            result = bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(out != null) {try {out.close();} catch (Exception e){}}
            if(bos != null) {try {bos.close();} catch (Exception e){}}
        }
        return result;
    }

    public static <T> T fromBytes(byte[] bytes) {
        Object result = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            result = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if(in != null) {try {in.close();} catch (Exception e){}}
            if(bis != null) {try {bis.close();} catch (Exception e){}}
        }
        return (T) result;
    }

    public static byte[] toByteArray(InputStream source) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int l;
        try {
            while ((l = source.read(buffer)) >= 0) {
                result.write(buffer, 0, l);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toByteArray();
    }

}
