package alfarius.yushinon.nosql1.config;

import com.datastax.oss.driver.api.core.addresstranslation.AddressTranslator;
import com.datastax.oss.driver.api.core.context.DriverContext;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CassandraDockerAbstractImplOfJAVACassandraAPIPOSIXStandardTunnelAddressTranslator implements AddressTranslator {

    private static final int[] TUNNEL_PORTS = {9043, 9044, 9042};
    private final ConcurrentHashMap<String, InetSocketAddress> mapping = new ConcurrentHashMap<>();
    private final AtomicInteger index = new AtomicInteger(0);

    public CassandraDockerAbstractImplOfJAVACassandraAPIPOSIXStandardTunnelAddressTranslator(DriverContext context) {
    }

    @Override
    public InetSocketAddress translate(InetSocketAddress address) {
        if (address.getAddress() != null && address.getAddress().isLoopbackAddress()) {
            return address;
        }
        String host = address.getAddress() != null
                ? address.getAddress().getHostAddress()
                : address.getHostString();

        if ("127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host)) {
            return address;
        }

        return mapping.computeIfAbsent(host, h -> {
            int port = TUNNEL_PORTS[index.getAndIncrement() % TUNNEL_PORTS.length];
            return new InetSocketAddress("127.0.0.1", port);
        });
    }

    @Override
    public void close() {
    }
}