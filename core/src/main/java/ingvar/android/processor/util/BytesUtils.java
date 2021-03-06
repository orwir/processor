package ingvar.android.processor.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class BytesUtils {

    /**
     * Convert serializable object to bytes.
     *
     * @param object object
     * @return bytes array
     */
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

    /**
     * Convert bytes array to object.
     *
     * @param bytes object bytes
     * @param <T> object class
     * @return object
     */
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

    /**
     * Write stream to bytes array.
     *
     * @param source stream
     * @return bytes array
     */
    public static byte[] streamToBytes(InputStream source) {
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

    /**
     * Copy fields from parent object to new child object.
     *
     * @param parent parent object
     * @param childClass child class
     * @param <T> child class
     * @return filled child object
     */
    public static <T> T shallowCopy(Object parent, Class<T> childClass) {
        try {
            return shallowCopy(parent, childClass.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copy fields from parent object to child object.
     *
     * @param parent parent object
     * @param child child object
     * @param <T> child class
     * @return filled child object
     */
    public static <T> T shallowCopy(Object parent, T child) {
        try {
            List<Field> fields = new ArrayList<>();
            Class clazz = parent.getClass();
            do {
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            } while (!(clazz = clazz.getSuperclass()).equals(Object.class));

            for (Field field : fields) {
                field.setAccessible(true);
                field.set(child, field.get(parent));
            }

            return child;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
