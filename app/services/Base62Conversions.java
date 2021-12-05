package services;

import io.seruco.encoding.base62.Base62;

import java.nio.ByteBuffer;

class Base62Conversions {
    private static Base62 base62 = Base62.createInstance();

    public static String encode(Long value) {
        ByteBuffer longBytes = ByteBuffer.allocate(Long.BYTES);
        longBytes.putLong(value);

        byte[] encodedBytes = base62.encode(longBytes.array());
        String encodedBytesString = new String(encodedBytes);

        return encodedBytesString.replaceAll("^0+", "");
    }

    public static Long decode(String value) {
        byte[] encodedBytes = value.getBytes();
        byte[] decodedBytes = base62.decode(encodedBytes);

        ByteBuffer longBytes = ByteBuffer.allocate(Long.BYTES);

        if (decodedBytes.length < Long.BYTES) {
            byte[] paddingBytes = new byte[Long.BYTES - decodedBytes.length];
            longBytes.put(paddingBytes);
        }

        longBytes.put(decodedBytes);
        longBytes.flip();

        return longBytes.getLong();
    }
}
