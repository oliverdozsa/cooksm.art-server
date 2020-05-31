package services;

import io.seruco.encoding.base62.Base62;

import java.math.BigInteger;

class Base62Conversions {
    private static Base62 base62 = Base62.createInstance();

    public static String encode(Long value) {
        BigInteger bigInt = BigInteger.valueOf(value);
        byte[] bigIntBytes = bigInt.toByteArray();
        byte[] encodedBytes = base62.encode(bigIntBytes);
        return new String(encodedBytes);
    }

    public static Long decode(String value) {
        byte[] encodedBytes = value.getBytes();
        byte[] decodedBytes = base62.decode(encodedBytes);
        BigInteger bigInt = new BigInteger(decodedBytes);
        return bigInt.longValue();
    }
}
