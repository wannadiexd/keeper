package com.keeper.db;

import com.keeper.model.Entry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String DB_PATH = System.getProperty("user.home") + "/Keeper/keeper.db";
    private Connection conn;

    public Database() throws SQLException {
        new java.io.File(System.getProperty("user.home") + "/Keeper").mkdirs();
        conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        init();
    }

    private void init() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS entries (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                title    TEXT NOT NULL,
                username TEXT,
                password TEXT,
                url      TEXT,
                category TEXT
            )
        """;
        conn.createStatement().execute(sql);
    }

    public void add(Entry e) throws SQLException {
        String sql = "INSERT INTO entries (title, username, password, url, category) VALUES (?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, e.getTitle());
        ps.setString(2, e.getUsername());
        ps.setString(3, e.getPassword());
        ps.setString(4, e.getUrl());
        ps.setString(5, e.getCategory());
        ps.executeUpdate();
    }

    public void update(Entry e) throws SQLException {
        String sql = "UPDATE entries SET title=?, username=?, password=?, url=?, category=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, e.getTitle());
        ps.setString(2, e.getUsername());
        ps.setString(3, e.getPassword());
        ps.setString(4, e.getUrl());
        ps.setString(5, e.getCategory());
        ps.setInt(6, e.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM entries WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Entry> getAll() throws SQLException {
        List<Entry> list = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM entries ORDER BY title");
        while (rs.next()) {
            Entry e = new Entry(
                rs.getString("title"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("url"),
                rs.getString("category")
            );
            e.setId(rs.getInt("id"));
            list.add(e);
        }
        return list;
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}