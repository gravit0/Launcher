package launchserver.auth.hwid;


import launcher.helper.CommonHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.ListConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.auth.MySQLSourceConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MysqlHWIDHandler extends HWIDHandler {
    private final MySQLSourceConfig mySQLHolder;
    private final String query;
    private final String isBannedName;
    private final String loginName;
    private final String hddName,cpuName,biosName;
    private final String[] queryParams;
    private final String queryUpd;
    private final String[] queryParamsUpd;
    private final String queryBan;
    private final String[] queryParamsBan;
    private final String querySelect;
    private final String[] queryParamsSelect;

    public MysqlHWIDHandler(BlockConfigEntry block) {
        super(block);
        mySQLHolder = new MySQLSourceConfig("hwidHandlerPool", block);

        // Read query
        query = VerifyHelper.verify(block.getEntryValue("query", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "MySQL query can't be empty");
        queryParams = block.getEntry("queryParams", ListConfigEntry.class).
                stream(StringConfigEntry.class).toArray(String[]::new);
        isBannedName = VerifyHelper.verify(block.getEntryValue("isBannedName", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "isBannedName can't be empty");
        loginName = VerifyHelper.verify(block.getEntryValue("loginName", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "loginName can't be empty");
        hddName = VerifyHelper.verify(block.getEntryValue("hddName", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "hddName can't be empty");
        cpuName = VerifyHelper.verify(block.getEntryValue("cpuName", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "cpuName can't be empty");
        biosName = VerifyHelper.verify(block.getEntryValue("biosName", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "biosName can't be empty");

        queryUpd = VerifyHelper.verify(block.getEntryValue("queryUpd", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "MySQL queryUpd can't be empty");
        queryParamsUpd = block.getEntry("queryParamsUpd", ListConfigEntry.class).
                stream(StringConfigEntry.class).toArray(String[]::new);
        queryBan = VerifyHelper.verify(block.getEntryValue("queryBan", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "MySQL queryBan can't be empty");
        queryParamsBan = block.getEntry("queryParamsBan", ListConfigEntry.class).
                stream(StringConfigEntry.class).toArray(String[]::new);
        querySelect = VerifyHelper.verify(block.getEntryValue("querySelect", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "MySQL queryUpd can't be empty");
        queryParamsSelect = block.getEntry("queryParamsSelect", ListConfigEntry.class).
                stream(StringConfigEntry.class).toArray(String[]::new);
    }

    @Override
    public void check0(HWID hwid, String username) throws HWIDException {
        try {
            Connection c = mySQLHolder.getConnection();

            PreparedStatement s = c.prepareStatement(query);
            String[] replaceParams = {"hwid_hdd", String.valueOf(hwid.getHwid_hdd()), "hwid_cpu", String.valueOf(hwid.getHwid_cpu()), "hwid_bios", String.valueOf(hwid.getHwid_bios()),"login",username};
            for (int i = 0; i < queryParams.length; i++) {
                s.setString(i + 1, CommonHelper.replace(queryParams[i], replaceParams));
            }

            // Execute SQL query
            s.setQueryTimeout(MySQLSourceConfig.TIMEOUT);
            try (ResultSet set = s.executeQuery()) {
                if(!set.next()) {
                    writeHWID(hwid,username,c);
                    return;
                }
                boolean isBanned = set.getBoolean(isBannedName);
                if(isBanned) throw new HWIDException("You HWID Banned");
                String login = set.getString(loginName);
                if(!username.equals(login))
                {
                    writeHWID(hwid,username,c);
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void writeHWID(HWID hwid, String username, Connection c)
    {
        try (PreparedStatement a = c.prepareStatement(queryUpd)) {
            //IF
            String[] replaceParamsUpd = {"hwid_hdd", String.valueOf(hwid.getHwid_hdd()), "hwid_cpu", String.valueOf(hwid.getHwid_cpu()), "hwid_bios", String.valueOf(hwid.getHwid_bios()), "login", username};
            for (int i = 0; i < queryParamsUpd.length; i++) {
                a.setString(i + 1, CommonHelper.replace(queryParamsUpd[i], replaceParamsUpd));
            }
            a.setQueryTimeout(MySQLSourceConfig.TIMEOUT);
            a.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void setIsBanned(HWID hwid,boolean isBanned)
    {
        Connection c = null;
        try {
            c = mySQLHolder.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (PreparedStatement a = c.prepareStatement(queryBan)) {
            //IF
            String[] replaceParamsUpd = {"hwid_hdd", String.valueOf(hwid.getHwid_hdd()), "hwid_cpu", String.valueOf(hwid.getHwid_cpu()), "hwid_bios", String.valueOf(hwid.getHwid_bios()), "isBanned", isBanned ? "1" : "0"};
            for (int i = 0; i < queryParamsBan.length; i++) {
                a.setString(i + 1, CommonHelper.replace(queryParamsBan[i], replaceParamsUpd));
            }
            a.setQueryTimeout(MySQLSourceConfig.TIMEOUT);
            a.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void ban(List<HWID> list) throws HWIDException {

        for(HWID hwid : list)
        {
            setIsBanned(hwid,true);
        }
    }

    @Override
    public void unban(List<HWID> list) throws HWIDException {
        for(HWID hwid : list)
        {
            setIsBanned(hwid,false);
        }
    }

    @Override
    public List<HWID> getHwid(String username) throws HWIDException {
        try {
            Connection c = mySQLHolder.getConnection();
            PreparedStatement s = c.prepareStatement(querySelect);
            String[] replaceParams = {"login", username};
            for (int i = 0; i < queryParams.length; i++) {
                s.setString(i + 1, CommonHelper.replace(queryParamsSelect[i], replaceParams));
            }
            long hdd,cpu,bios;
            try (ResultSet set = s.executeQuery()) {
                hdd = set.getLong(hddName);
                cpu = set.getLong(cpuName);
                bios = set.getLong(biosName);
            }
            ArrayList<HWID> list = new ArrayList<>();
            list.add(HWID.gen(hdd,bios,cpu));
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        // Do nothing
    }
}
