package launchserver.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import launcher.LauncherAPI;
import launcher.helper.CommonHelper;
import launcher.helper.LogHelper;
import launcher.managers.GarbageManager;
import launchserver.LaunchServer;
import launchserver.manangers.SessionManager;

public final class ServerSocketHandler implements Runnable, AutoCloseable {
    public interface Listener {
        @LauncherAPI
        boolean onConnect(InetAddress address);

        @LauncherAPI
        void onDisconnect(Exception e);

        @LauncherAPI
        boolean onHandshake(long session, Integer type);
    }
    private static final ThreadFactory THREAD_FACTORY = r -> CommonHelper.newThread("Network Thread", true, r);

    @LauncherAPI
    public volatile boolean logConnections = Boolean.getBoolean("launcher.logConnections");
    // Instance
    private final LaunchServer server;
    private final AtomicReference<ServerSocket> serverSocket = new AtomicReference<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool(THREAD_FACTORY);

    public final SessionManager sessionManager;
    private final AtomicLong idCounter = new AtomicLong(0L);

    private volatile Listener listener;

    public ServerSocketHandler(LaunchServer server) {
        this.server = server;
        sessionManager = new SessionManager();
        GarbageManager.registerNeedGC(sessionManager);
    }

    public ServerSocketHandler(LaunchServer server, SessionManager sessionManager) {
        this.server = server;
        this.sessionManager = sessionManager;
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

    /*package*/ void onDisconnect(Exception e) {
        if (listener != null)
			listener.onDisconnect(e);
    }

    /*package*/ boolean onHandshake(long session, Integer type) {
        return listener == null || listener.onHandshake(session, type);
    }

    @Override
    public void run() {
        LogHelper.info("Starting server socket thread");
        try (ServerSocket serverSocket = new ServerSocket()) {
            if (!this.serverSocket.compareAndSet(null, serverSocket))
				throw new IllegalStateException("Previous socket wasn't closed");

            // Set socket params
            serverSocket.setReuseAddress(true);
            serverSocket.setPerformancePreferences(1, 0, 2);
            //serverSocket.setReceiveBufferSize(0x10000);
            serverSocket.bind(server.config.getSocketAddress());
            LogHelper.info("Server socket thread successfully started");

            // Listen for incoming connections
            while (serverSocket.isBound()) {
                Socket socket = serverSocket.accept();

                // Invoke pre-connect listener
                long id = idCounter.incrementAndGet();
                if (listener != null && !listener.onConnect(socket.getInetAddress()))
					continue; // Listener didn't accepted this connection

                // Reply in separate thread
                threadPool.execute(new ResponseThread(server, id, socket, sessionManager));
            }
        } catch (IOException e) {
            // Ignore error after close/rebind
            if (serverSocket.get() != null)
				LogHelper.error(e);
        }
    }

    @LauncherAPI
    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
