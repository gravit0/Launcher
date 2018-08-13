package launchserver.response;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import launcher.LauncherAPI;
import launcher.helper.CommonHelper;
import launcher.helper.LogHelper;
import launcher.helper.VerifyHelper;
import launcher.request.Request.Type;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.ssl.LauncherKeyStore;
import launcher.ssl.LauncherTrustManager;
import launchserver.LaunchServer;
import launchserver.response.Response.Factory;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;

public final class ServerSocketHandler implements Runnable, AutoCloseable {
    private static SSLServerSocketFactory ssf;
    private static final ThreadFactory THREAD_FACTORY = r -> CommonHelper.newThread("Network Thread", true, r);
    @LauncherAPI public volatile boolean logConnections = Boolean.getBoolean("launcher.logConnections");

    // Instance
    private final LaunchServer server;
    private final AtomicReference<ServerSocket> serverSocket = new AtomicReference<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool(THREAD_FACTORY);

    // API
    private final Map<String, Factory> customResponses = new ConcurrentHashMap<>(2);
    private final AtomicLong idCounter = new AtomicLong(0L);
    private volatile Listener listener;

    public ServerSocketHandler(LaunchServer server) {
        this.server = server;
    }

    @Override
    public void close() {
        ServerSocket socket = serverSocket.getAndSet(null);
        if (socket != null) {
            LogHelper.info("Closing server socket listener");
            try {
                socket.close();
            } catch (IOException e) {
                LogHelper.error(e);
            }
        }
    }

    public void SSLContextInit() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException, IOException, CertificateException {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new LauncherTrustManager()
        };
        KeyStore ks = LauncherKeyStore.getKeyStore("keystore","PSP1000");

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(ks, "PSP1000".toCharArray());
        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
        ssf = sc.getServerSocketFactory();
    }

    @Override
    public void run() {
        try {
            SSLContextInit();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        //System.setProperty( "javax.net.ssl.keyStore","keystore");
        //System.setProperty( "javax.net.ssl.keyStorePassword","PSP1000");
        LogHelper.info("Starting server socket thread");
        try (SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket()) {
            serverSocket.setEnabledProtocols(new String[] {"TLSv1.2"});
            if (!this.serverSocket.compareAndSet(null, serverSocket)) {
                throw new IllegalStateException("Previous socket wasn't closed");
            }

            // Set socket params
            serverSocket.setReuseAddress(true);
            serverSocket.setPerformancePreferences(1, 0, 2);
            //serverSocket.setReceiveBufferSize(0x10000);
            serverSocket.bind(server.config.getSocketAddress());
            LogHelper.info("Server socket thread successfully started");
            // Listen for incoming connections
            while (serverSocket.isBound()) {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                socket.startHandshake();
                // Invoke pre-connect listener
                long id = idCounter.incrementAndGet();
                if (listener != null && !listener.onConnect(id, socket.getInetAddress())) {
                    continue; // Listener didn't accepted this connection
                }

                // Reply in separate thread
                threadPool.execute(new ResponseThread(server, id, socket));
            }
        } catch (IOException e) {
            // Ignore error after close/rebind
            if (serverSocket.get() != null) {
                LogHelper.error(e);
            }
        }
    }

    @LauncherAPI
    public Response newCustomResponse(String name, long id, HInput input, HOutput output) {
        Factory factory = VerifyHelper.getMapValue(customResponses, name,
            String.format("Unknown custom response: '%s'", name));
        return factory.newResponse(server, id, input, output);
    }

    @LauncherAPI
    public void registerCustomResponse(String name, Factory factory) {
        VerifyHelper.verifyIDName(name);
        VerifyHelper.putIfAbsent(customResponses, name, Objects.requireNonNull(factory, "factory"),
            String.format("Custom response has been already registered: '%s'", name));
    }

    @LauncherAPI
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /*package*/ void onDisconnect(long id, Exception e) {
        if (listener != null) {
            listener.onDisconnect(id, e);
        }
    }

    /*package*/ boolean onHandshake(long id, Type type) {
        return listener == null || listener.onHandshake(id, type);
    }

    public interface Listener {
        @LauncherAPI
        boolean onConnect(long id, InetAddress address);

        @LauncherAPI
        void onDisconnect(long id, Exception e);

        @LauncherAPI
        boolean onHandshake(long id, Type type);
    }
}