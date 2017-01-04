package com.library.web.beans;

import com.library.web.db.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean
@ApplicationScoped
public class Genres {
    
    private List<Genre> genreList = new ArrayList<>();
    
    public List<Genre> getGenreList() {
        if (genreList.isEmpty()) {
            return getGeners();
        }
        return genreList;
    }
    
    private List<Genre> getGeners() {
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from genre  order by name");
            while (rs.next()) {
                Genre genre = new Genre();
                genre.setId(rs.getLong("id"));
                genre.setName(rs.getString("name"));
                genreList.add(genre);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return genreList;
    }    
}
