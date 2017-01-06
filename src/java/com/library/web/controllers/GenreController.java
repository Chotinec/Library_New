package com.library.web.controllers;

import com.library.web.beans.Genre;
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

@ManagedBean(eager = true)
@ApplicationScoped
public class GenreController {
    
    private List<Genre> genreList = new ArrayList<>();
    
    public GenreController() {
        fillGenresAll();
    }
    
    public List<Genre> getGenreList() {
        return genreList;
    }
    
    private void fillGenresAll() {
        
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
            Logger.getLogger(GenreController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(GenreController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
