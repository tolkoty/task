package com.example.cache.pg;

import com.example.cache.events.CacheEventBus;
import com.example.cache.events.EntityChangedEvent;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

@Component
public class PgNotifyListener implements InitializingBean, DisposableBean, Runnable {
    private final DataSource dataSource;
    private final CacheEventBus eventBus;
    private final String channel;
    private volatile boolean running = false;
    private Thread thread;

    public PgNotifyListener(DataSource dataSource,
                            CacheEventBus eventBus,
                            @Value("${cache.pg.channel:cache_changes}") String channel) {
        this.dataSource = dataSource;
        this.eventBus = eventBus;
        this.channel = channel;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        running = true;
        thread = new Thread(this, "pg-notify-listener");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void destroy() throws Exception {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(true);
            try (Statement st = connection.createStatement()) {
                st.execute("LISTEN " + channel);
            }
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            while (running) {
                PGNotification[] notifications = pgConnection.getNotifications(10_000);
                if (notifications != null) {
                    for (PGNotification n : notifications) {
                        String payload = n.getParameter();
                        // expected format: table:event:id   e.g. users:UPDATED:42
                        String[] parts = payload.split(":");
                        if (parts.length == 3) {
                            String table = parts[0];
                            EntityChangedEvent.ChangeType type = EntityChangedEvent.ChangeType.valueOf(parts[1]);
                            String idStr = parts[2];
                            Long id = null;
                            try { id = Long.valueOf(idStr); } catch (NumberFormatException ignore) {}
                            Object idObj = id != null ? id : idStr;
                            eventBus.publish(new EntityChangedEvent(table, type, idObj));
                        }
                    }
                }
                try { Thread.sleep(Duration.ofSeconds(1).toMillis()); } catch (InterruptedException ignored) {}
            }
        } catch (SQLException e) {
            // log and exit; Spring will manage bean lifecycle
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try { DataSourceUtils.releaseConnection(connection, dataSource); } catch (Exception ignore) {}
            }
        }
    }
}
