package com.library.web.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Database {
    
    private static Connection conn;
    private static InitialContext ic;
    private static DataSource ds;
    
    private static final String PATH = "jdbc/Library";
    
    public static Connection getConnection() {
        
        try {
            ic = new InitialContext();
            ds = (DataSource)ic.lookup(PATH);
            conn = ds.getConnection();
        } catch (NamingException | SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        return conn;
    }
    
}
