package com.keeper.db;

import com.keeper.crypto.Crypto;
import com.keeper.model.Entry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String DB_PATH = System.getProperty("user.home") + "/Keeper/keeper.db";
    private Connection conn;
    private Crypto crypto;

    public Database(Crypto crypto) throws SQLException {
        this.crypto = crypto;
        new java.io.File(System.getProperty("user.home") + "/Keeper").mkdirs();
        conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        init();
    }

    private void init() throws SQLException {
        conn.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS entries (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                title    TEXT NOT NULL,
                username TEXT,
                password TEXT,
                url      TEXT,
                notes    TEXT,
                category TEXT
            )
        """);
        conn.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS meta (
                key   TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """);
    }

    public void add(Entry e) throws Exception {
        String sql = "INSERT INTO entries (title, username, password, url, notes, category) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, e.getTitle());
        ps.setString(2, e.getUsername());
        ps.setString(3, crypto.encrypt(e.getPassword()));
        ps.setString(4, e.getUrl());
        ps.setString(5, e.getNotes());
        ps.setString(6, e.getCategory());
        ps.executeUpdate();
    }

    public void update(Entry e) throws Exception {
        String sql = "UPDATE entries SET title=?, username=?, password=?, url=?, notes=?, category=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, e.getTitle());
        ps.setString(2, e.getUsername());
        ps.setString(3, crypto.encrypt(e.getPassword()));
        ps.setString(4, e.getUrl());
        ps.setString(5, e.getNotes());
        ps.setString(6, e.getCategory());
        ps.setInt(7, e.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM entries WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Entry> getAll() throws Exception {
        List<Entry> list = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM entries ORDER BY title");
        while (rs.next()) {
            Entry e = new Entry(
                rs.getString("title"),
                rs.getString("username"),
                crypto.decrypt(rs.getString("password")),
                rs.getString("url"),
                rs.getString("notes"),
                rs.getString("category")
            );
            e.setId(rs.getInt("id"));
            list.add(e);
        }
        return list;
    }

    public void setMeta(String key, String value) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT OR REPLACE INTO meta (key, value) VALUES (?, ?)");
        ps.setString(1, key);
        ps.setString(2, value);
        ps.executeUpdate();
    }

    public String getMeta(String key) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT value FROM meta WHERE key = ?");
        ps.setString(1, key);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString("value") : null;
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}